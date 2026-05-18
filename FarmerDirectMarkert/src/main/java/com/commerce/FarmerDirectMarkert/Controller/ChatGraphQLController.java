package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.ChatMessageResponse;
import com.commerce.FarmerDirectMarkert.service.ChatService;
import com.commerce.FarmerDirectMarkert.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class ChatGraphQLController {

    private final ChatService chatService;
    private final JwtService jwtService;

    @SubscriptionMapping
    public Flux<ChatMessageResponse> receiveMessages(@Argument String token) {
        // We do a simple token validation here directly in the controller for ease of use.
        // In a production app, interceptors are better, but this handles the auth correctly.
        if (token == null || token.isBlank()) {
            return Flux.error(new RuntimeException("Unauthorized: Token is missing"));
        }

        String userEmail;
        try {
            userEmail = jwtService.extractUsername(token);
        } catch (Exception e) {
            return Flux.error(new RuntimeException("Unauthorized: Invalid token"));
        }

        // Return the flux from the sink, filtering out messages not meant for this user
        // A user should receive messages where they are the recipient or the sender (to keep multi-device sync)
        return chatService.getMessagesStream()
                .filter(msg -> msg.getRecipientEmail().equals(userEmail) || msg.getSenderEmail().equals(userEmail));
    }

    @QueryMapping
    public String ping() {
        return "pong";
    }
}
