package com.commerce.FarmerDirectMarkert.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageApprovalRequestDto {

    @NotBlank(message = "Recipient email is required")
    private String recipientEmail;

    @NotBlank(message = "Message content is required")
    private String messageContent;
}
