package com.commerce.FarmerDirectMarkert.service;

import com.commerce.FarmerDirectMarkert.dto.ChatMessageRequest;
import com.commerce.FarmerDirectMarkert.dto.ChatMessageResponse;
import com.commerce.FarmerDirectMarkert.dto.ChatUserDto;
import com.commerce.FarmerDirectMarkert.dto.MessageApprovalRequestDto;
import com.commerce.FarmerDirectMarkert.dto.MessageApprovalResponseDto;
import com.commerce.FarmerDirectMarkert.model.MessageApprovalRequest;
import com.commerce.FarmerDirectMarkert.model.MessageApprovalRequest.ApprovalStatus;
import com.commerce.FarmerDirectMarkert.model.User;
import com.commerce.FarmerDirectMarkert.repository.MessageApprovalRequestRepository;
import com.commerce.FarmerDirectMarkert.repository.UserRepository;
import com.commerce.FarmerDirectMarkert.websocket.ChatWebSocketHandler;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final MessageApprovalRequestRepository approvalRequestRepository;
    
    // Sink to broadcast messages for GraphQL subscriptions
    private final Sinks.Many<ChatMessageResponse> chatMessageSink = Sinks.many().multicast().onBackpressureBuffer();

    public Flux<ChatMessageResponse> getMessagesStream() {
        return chatMessageSink.asFlux();
    }

    public ChatMessageResponse sendMessage(String senderEmail, ChatMessageRequest request) throws IOException {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Sender account was not found."));

        User recipient = userRepository.findByEmail(request.getRecipientEmail())
                .orElseThrow(() -> new IllegalArgumentException("Recipient account was not found."));

        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (content.isBlank()) {
            throw new IllegalArgumentException("Message content is required.");
        }

        // Build the payload once so the same message can be returned by REST and pushed by websocket.
        ChatMessageResponse response = ChatMessageResponse.builder()
                .messageId(UUID.randomUUID().toString())
                .senderEmail(sender.getEmail())
                .senderName(sender.getFullName())
                .senderRole(sender.getRole())
                .recipientEmail(recipient.getEmail())
                .content(content)
                .sentAt(Instant.now())
                .build();

        chatWebSocketHandler.sendChatMessage(response);
        // Also emit to the GraphQL sink
        chatMessageSink.tryEmitNext(response);
        
        return response;
    }

    public boolean isUserOnline(String email) {
        return chatWebSocketHandler.isUserOnline(email);
    }

    public List<ChatUserDto> getChatUsers(String currentUserEmail) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getEmail().equalsIgnoreCase(currentUserEmail))
                .sorted(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER))
                .map(user -> ChatUserDto.builder()
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .online(chatWebSocketHandler.isUserOnline(user.getEmail()))
                        .build())
                .toList();
    }

    // ==================== Message Approval Request Methods ====================

    /**
     * Create a message approval request before sending the actual message.
     * The recipient will need to approve this request before the message can be sent.
     * 
     * @param senderEmail Email of the user requesting to send the message
     * @param request Contains recipient email and message content
     * @return MessageApprovalResponseDto containing the approval request details
     */
    public MessageApprovalResponseDto createMessageApprovalRequest(
            String senderEmail,
            MessageApprovalRequestDto request) {
        
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Sender account was not found."));

        User recipient = userRepository.findByEmail(request.getRecipientEmail())
                .orElseThrow(() -> new IllegalArgumentException("Recipient account was not found."));

        String content = request.getMessageContent() == null ? "" : request.getMessageContent().trim();
        if (content.isBlank()) {
            throw new IllegalArgumentException("Message content is required.");
        }

        // Create a new approval request
        MessageApprovalRequest approvalRequest = MessageApprovalRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .messageContent(content)
                .status(ApprovalStatus.PENDING)
                .build();

        approvalRequest = approvalRequestRepository.save(approvalRequest);

        return mapToApprovalResponseDto(approvalRequest, sender);
    }

    /**
     * Get all pending approval requests for a specific recipient.
     * Only the recipient can see pending requests meant for them.
     * 
     * @param recipientEmail Email of the user receiving the requests
     * @return List of pending message approval requests
     */
    public List<MessageApprovalResponseDto> getPendingApprovalRequests(String recipientEmail) {
        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new IllegalArgumentException("User account was not found."));

        return approvalRequestRepository
                .findByRecipientAndStatus(recipient, ApprovalStatus.PENDING)
                .stream()
                .map(req -> mapToApprovalResponseDto(req, req.getSender()))
                .toList();
    }

    /**
     * Get all approval request history for a specific user (both sent and received).
     * 
     * @param userEmail Email of the user
     * @return List of all approval requests (sent and received)
     */
    public List<MessageApprovalResponseDto> getApprovalRequestHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User account was not found."));

        return approvalRequestRepository
                .findByRecipient(user)
                .stream()
                .map(req -> mapToApprovalResponseDto(req, req.getSender()))
                .toList();
    }

    /**
     * Approve a message approval request and send the message.
     * Only the recipient can approve a request.
     * 
     * @param approvalRequestId The ID of the approval request to approve
     * @param recipientEmail Email of the recipient approving the request
     * @return ChatMessageResponse containing the sent message details
     */
    public ChatMessageResponse approveMessageRequest(
            String approvalRequestId,
            String recipientEmail) throws IOException {
        
        MessageApprovalRequest approvalRequest = approvalRequestRepository.findById(approvalRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found."));

        // Verify the recipient is the one approving
        if (!approvalRequest.getRecipient().getEmail().equals(recipientEmail)) {
            throw new IllegalArgumentException("Only the recipient can approve this request.");
        }

        // Check if request is still pending
        if (!approvalRequest.getStatus().equals(ApprovalStatus.PENDING)) {
            throw new IllegalArgumentException("This request has already been " + 
                    approvalRequest.getStatus().toString().toLowerCase() + ".");
        }

        // Update approval request status
        approvalRequest.setStatus(ApprovalStatus.APPROVED);
        approvalRequest.setApprovedAt(Instant.now());
        approvalRequestRepository.save(approvalRequest);

        // Now send the actual message
        ChatMessageRequest messageRequest = ChatMessageRequest.builder()
                .recipientEmail(approvalRequest.getRecipient().getEmail())
                .content(approvalRequest.getMessageContent())
                .build();

        return sendMessage(approvalRequest.getSender().getEmail(), messageRequest);
    }

    /**
     * Reject a message approval request.
     * Only the recipient can reject a request.
     * 
     * @param approvalRequestId The ID of the approval request to reject
     * @param recipientEmail Email of the recipient rejecting the request
     * @param rejectionReason Optional reason for rejection
     * @return MessageApprovalResponseDto containing the updated request details
     */
    public MessageApprovalResponseDto rejectMessageRequest(
            String approvalRequestId,
            String recipientEmail,
            String rejectionReason) {
        
        MessageApprovalRequest approvalRequest = approvalRequestRepository.findById(approvalRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found."));

        // Verify the recipient is the one rejecting
        if (!approvalRequest.getRecipient().getEmail().equals(recipientEmail)) {
            throw new IllegalArgumentException("Only the recipient can reject this request.");
        }

        // Check if request is still pending
        if (!approvalRequest.getStatus().equals(ApprovalStatus.PENDING)) {
            throw new IllegalArgumentException("This request has already been " + 
                    approvalRequest.getStatus().toString().toLowerCase() + ".");
        }

        // Update approval request status
        approvalRequest.setStatus(ApprovalStatus.REJECTED);
        approvalRequest.setRejectedAt(Instant.now());
        if (rejectionReason != null && !rejectionReason.trim().isEmpty()) {
            approvalRequest.setRejectionReason(rejectionReason.trim());
        }
        approvalRequest = approvalRequestRepository.save(approvalRequest);

        return mapToApprovalResponseDto(approvalRequest, approvalRequest.getSender());
    }

    /**
     * Helper method to map MessageApprovalRequest entity to MessageApprovalResponseDto
     */
    private MessageApprovalResponseDto mapToApprovalResponseDto(
            MessageApprovalRequest request,
            User sender) {
        return MessageApprovalResponseDto.builder()
                .approvalRequestId(request.getId())
                .senderEmail(sender.getEmail())
                .senderName(sender.getFullName())
                .senderRole(sender.getRole().toString())
                .recipientEmail(request.getRecipient().getEmail())
                .messageContent(request.getMessageContent())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .approvedAt(request.getApprovedAt())
                .rejectedAt(request.getRejectedAt())
                .rejectionReason(request.getRejectionReason())
                .build();
    }
}
