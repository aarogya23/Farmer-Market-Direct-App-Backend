package com.commerce.FarmerDirectMarkert.dto;

import com.commerce.FarmerDirectMarkert.model.AdminNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminNotificationDto {

    private Long id;
    private String adminName;
    private String title;
    private String message;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String additionalInfo;
    private Integer totalRecipients;
    private Boolean isRead;
    private List<String> targetRoles;

    public static AdminNotificationDto fromEntity(AdminNotification notification, Boolean isRead) {
        return AdminNotificationDto.builder()
                .id(notification.getId())
                .adminName(notification.getAdmin().getFullName())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().toString())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .additionalInfo(notification.getAdditionalInfo())
                .totalRecipients(notification.getTotalRecipients())
                .isRead(isRead)
                .targetRoles(notification.getTargetRoles())
                .build();
    }
}
