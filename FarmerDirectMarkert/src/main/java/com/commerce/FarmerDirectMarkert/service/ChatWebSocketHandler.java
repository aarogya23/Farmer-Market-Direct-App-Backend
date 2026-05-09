package com.commerce.FarmerDirectMarkert.websocket;

import com.commerce.FarmerDirectMarkert.dto.ChatMessageResponse;
import com.commerce.FarmerDirectMarkert.service.Customuserdetailsservice;
import com.commerce.FarmerDirectMarkert.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final Customuserdetailsservice userDetailsService;

    // We keep only the latest open session for each user email so messages can be routed quickly.
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userEmail = authenticateSession(session);
        if (userEmail == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized websocket session"));
            return;
        }

        // Store the authenticated user's session for later direct message delivery.
        activeSessions.put(userEmail, session);
        sendSystemMessage(session, "CONNECTED", "WebSocket connection established.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // To keep the websocket layer simple, sending is done through a REST API.
        sendSystemMessage(session, "INFO", "Use the chat REST API to send messages.");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userEmail = getUserEmail(session);
        if (userEmail != null) {
            activeSessions.remove(userEmail, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sendSystemMessage(session, "ERROR", exception.getMessage() != null ? exception.getMessage() : "WebSocket error");
    }

    public void sendChatMessage(ChatMessageResponse response) throws IOException {
        String payload = objectMapper.writeValueAsString(response);

        // Push the same message to both sides so each client stays in sync.
        sendToUser(response.getSenderEmail(), payload);
        sendToUser(response.getRecipientEmail(), payload);
    }

    public boolean isUserOnline(String email) {
        WebSocketSession session = activeSessions.get(email);
        return session != null && session.isOpen();
    }

    private String getUserEmail(WebSocketSession session) {
        Object userEmail = session.getAttributes().get("userEmail");
        return userEmail instanceof String ? (String) userEmail : null;
    }

    private String authenticateSession(WebSocketSession session) {
        // Keep auth local to the handler so the websocket flow stays easy to follow.
        // The frontend sends the JWT as a query parameter: /ws/chat?token=<jwt>
        String token = extractToken(session.getUri());
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(token, userDetails)) {
                return null;
            }

            session.getAttributes().put("userEmail", userDetails.getUsername());
            return userDetails.getUsername();
        } catch (Exception exception) {
            return null;
        }
    }

    private String extractToken(URI uri) {
        if (uri == null || uri.getQuery() == null || uri.getQuery().isBlank()) {
            return null;
        }

        // Read only the token query parameter and ignore any others.
        for (String queryPart : uri.getQuery().split("&")) {
            String[] pair = queryPart.split("=", 2);
            if (pair.length == 2 && "token".equals(pair[0]) && !pair[1].isBlank()) {
                return pair[1];
            }
        }

        return null;
    }

    private void sendToUser(String email, String payload) throws IOException {
        WebSocketSession session = activeSessions.get(email);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(payload));
        }
    }

    private void sendSystemMessage(WebSocketSession session, String type, String message) throws IOException {
        if (!session.isOpen()) {
            return;
        }

        // System messages give the client one predictable format for connect/error feedback.
        String payload = objectMapper.writeValueAsString(Map.of(
                "type", type,
                "message", message,
                "timestamp", Instant.now().toString()
        ));
        session.sendMessage(new TextMessage(payload));
    }
}
