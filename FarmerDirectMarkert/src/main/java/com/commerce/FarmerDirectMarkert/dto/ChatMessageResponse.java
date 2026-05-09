package com.commerce.FarmerDirectMarkert.dto;

import com.commerce.FarmerDirectMarkert.model.User.Role;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private String messageId;
    private String senderEmail;
    private String senderName;
    private Role senderRole;
    private String recipientEmail;
    private String content;
    private Instant sentAt;
}
