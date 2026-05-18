package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.ChatMessageRequest;
import com.commerce.FarmerDirectMarkert.dto.ChatMessageResponse;
import com.commerce.FarmerDirectMarkert.dto.ChatUserDto;
import com.commerce.FarmerDirectMarkert.dto.ConversationResponseDto;
import com.commerce.FarmerDirectMarkert.dto.MessageApprovalRequestDto;
import com.commerce.FarmerDirectMarkert.dto.MessageApprovalResponseDto;
import com.commerce.FarmerDirectMarkert.service.ChatService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatMessageRequest request
    ) throws IOException {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }

        // The logged-in user becomes the sender automatically.
        ChatMessageResponse response = chatService.sendMessage(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/online/{email}")
    public ResponseEntity<Map<String, Object>> isUserOnline(@PathVariable String email) {
        return ResponseEntity.ok(Map.of(
                "email", email,
                "online", chatService.isUserOnline(email)
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<List<ChatUserDto>> getChatUsers(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }

        return ResponseEntity.ok(chatService.getChatUsers(userDetails.getUsername()));
    }

    @GetMapping("/conversation/{email}")
    public ResponseEntity<ConversationResponseDto> getConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String email
    ) {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }

        try {
            String approvalStatus = chatService.getApprovalStatus(userDetails.getUsername(), email);
            List<ChatMessageResponse> messages = chatService.getConversationHistory(userDetails.getUsername(), email);
            
            return ResponseEntity.ok(ConversationResponseDto.builder()
                    .approvalStatus(approvalStatus)
                    .messages(messages)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== Message Approval Request Endpoints ====================


    @PostMapping("/approval-request/create")
    public ResponseEntity<MessageApprovalResponseDto> createMessageApprovalRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MessageApprovalRequestDto request
    ) {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }

        try {
            MessageApprovalResponseDto response = chatService.createMessageApprovalRequest(userDetails.getUsername(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/approval-request/pending")
    public ResponseEntity<List<MessageApprovalResponseDto>> getPendingApprovalRequests(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }

        try {
            List<MessageApprovalResponseDto> requests = chatService
                    .getPendingApprovalRequests(userDetails.getUsername());
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get approval request history for the current user.
     * Shows all approval requests (sent and received).
     * 
     * @param userDetails Authenticated user
     * @return List of MessageApprovalResponseDto
     */
    @GetMapping("/approval-request/history")
    public ResponseEntity<List<MessageApprovalResponseDto>> getApprovalRequestHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }

        try {
            List<MessageApprovalResponseDto> history = chatService
                    .getApprovalRequestHistory(userDetails.getUsername());
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Approve a message approval request and send the message.
     * Only the recipient can approve.
     * 
     * @param userDetails Authenticated user (recipient)
     * @param approvalRequestId ID of the approval request to approve
     * @return ChatMessageResponse if approved and message sent successfully
     */
    @PostMapping("/approval-request/{approvalRequestId}/approve")
    public ResponseEntity<ChatMessageResponse> approveMessageRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String approvalRequestId
    ) {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }

        try {
            ChatMessageResponse response = chatService
                    .approveMessageRequest(approvalRequestId, userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reject a message approval request.
     * Only the recipient can reject. Optional rejection reason can be provided.
     * 
     * @param userDetails Authenticated user (recipient)
     * @param approvalRequestId ID of the approval request to reject
     * @param rejectionReason Optional reason for rejection
     * @return MessageApprovalResponseDto with updated request status
     */
    @PostMapping("/approval-request/{approvalRequestId}/reject")
    public ResponseEntity<MessageApprovalResponseDto> rejectMessageRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String approvalRequestId,
            @RequestParam(required = false) String rejectionReason
    ) {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }

        try {
            MessageApprovalResponseDto response = chatService
                    .rejectMessageRequest(approvalRequestId, userDetails.getUsername(), rejectionReason);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
