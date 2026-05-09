package com.commerce.FarmerDirectMarkert.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be valid")
    private String recipientEmail;

    @NotBlank(message = "Message content is required")
    private String content;
}
