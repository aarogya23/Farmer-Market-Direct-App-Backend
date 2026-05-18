package com.commerce.FarmerDirectMarkert.dto;

import com.commerce.FarmerDirectMarkert.model.MessageApprovalRequest.ApprovalStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageApprovalResponseDto {

    private String approvalRequestId;
    private String senderEmail;
    private String senderName;
    private String senderRole;
    private String recipientEmail;
    private String messageContent;
    private ApprovalStatus status;
    private Instant createdAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private String rejectionReason;
}
