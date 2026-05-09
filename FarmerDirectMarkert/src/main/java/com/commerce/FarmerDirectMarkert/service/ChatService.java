package com.commerce.FarmerDirectMarkert.service;

import com.commerce.FarmerDirectMarkert.dto.ChatMessageRequest;
import com.commerce.FarmerDirectMarkert.dto.ChatMessageResponse;
import com.commerce.FarmerDirectMarkert.model.User;
import com.commerce.FarmerDirectMarkert.repository.UserRepository;
import com.commerce.FarmerDirectMarkert.websocket.ChatWebSocketHandler;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatWebSocketHandler chatWebSocketHandler;

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
        return response;
    }

    public boolean isUserOnline(String email) {
        return chatWebSocketHandler.isUserOnline(email);
    }
}
