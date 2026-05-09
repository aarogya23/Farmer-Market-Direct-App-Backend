package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.ChatMessageRequest;
import com.commerce.FarmerDirectMarkert.dto.ChatMessageResponse;
import com.commerce.FarmerDirectMarkert.service.ChatService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
