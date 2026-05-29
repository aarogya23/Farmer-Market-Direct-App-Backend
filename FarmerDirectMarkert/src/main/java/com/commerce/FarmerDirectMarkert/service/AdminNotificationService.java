package com.commerce.FarmerDirectMarkert.service;

import com.commerce.FarmerDirectMarkert.dto.AdminNotificationDto;
import com.commerce.FarmerDirectMarkert.dto.SendNotificationRequest;
import com.commerce.FarmerDirectMarkert.model.AdminNotification;
import com.commerce.FarmerDirectMarkert.model.User;
import com.commerce.FarmerDirectMarkert.model.UserNotificationRead;
import com.commerce.FarmerDirectMarkert.repository.AdminNotificationRepository;
import com.commerce.FarmerDirectMarkert.repository.UserNotificationReadRepository;
import com.commerce.FarmerDirectMarkert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final AdminNotificationRepository notificationRepository;
    private final UserNotificationReadRepository notificationReadRepository;
    private final UserRepository userRepository;

    /**
     * Send notification to selected users or all users with specific roles
     */
    public AdminNotificationDto sendNotification(SendNotificationRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admins can send notifications");
        }

        List<String> recipientIds = new ArrayList<>();

        // If specific users are provided, use them
        if (request.getRecipientUserIds() != null && !request.getRecipientUserIds().isEmpty()) {
            recipientIds.addAll(request.getRecipientUserIds());
        } else if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            // Otherwise, find all users with target roles
            List<User> users = userRepository.findAll();
            recipientIds = users.stream()
                    .filter(u -> request.getTargetRoles().contains(u.getRole().toString()))
                    .map(User::getId)
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Either recipient user IDs or target roles must be provided");
        }

        AdminNotification notification = AdminNotification.builder()
                .admin(admin)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .recipientIds(recipientIds)
                .targetRoles(request.getTargetRoles() != null ? request.getTargetRoles() : new ArrayList<>())
                .additionalInfo(request.getAdditionalInfo())
                .totalRecipients(recipientIds.size())
                .build();

        AdminNotification savedNotification = notificationRepository.save(notification);
        return AdminNotificationDto.fromEntity(savedNotification, false);
    }

    /**
     * Get all notifications for a user (by email)
     */
    public List<AdminNotificationDto> getNotificationsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String userId = user.getId();
        List<AdminNotification> notifications = notificationRepository.findNotificationsForUser(userId);

        return notifications.stream()
                .map(notification -> {
                    Boolean isRead = notificationReadRepository
                            .findByNotificationIdAndUserId(notification.getId(), userId)
                            .isPresent();
                    return AdminNotificationDto.fromEntity(notification, isRead);
                })
                .collect(Collectors.toList());
    }

    /**
     * Mark a notification as read (by user email)
     */
    public void markNotificationAsRead(Long notificationId, String userEmail) {
        AdminNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is recipient
        if (!notification.getRecipientIds().contains(user.getId())) {
            throw new RuntimeException("User is not a recipient of this notification");
        }

        // Check if already marked as read
        Optional<UserNotificationRead> existing = notificationReadRepository
                .findByNotificationIdAndUserId(notificationId, user.getId());

        if (existing.isEmpty()) {
            UserNotificationRead read = UserNotificationRead.builder()
                    .notification(notification)
                    .user(user)
                    .build();
            notificationReadRepository.save(read);
        }
    }

    /**
     * Get notifications by role (e.g., get all notifications for FARMER role)
     */
    public List<AdminNotificationDto> getNotificationsByRole(String role) {
        List<AdminNotification> notifications = notificationRepository.findNotificationsByRole(role);
        return notifications.stream()
                .map(notification -> AdminNotificationDto.fromEntity(notification, false))
                .collect(Collectors.toList());
    }

    /**
     * Get count of unread notifications for a user (by email)
     */
    public Integer getUnreadNotificationCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String userId = user.getId();
        List<AdminNotification> notifications = notificationRepository.findNotificationsForUser(userId);
        return (int) notifications.stream()
                .filter(notification -> notificationReadRepository
                        .findByNotificationIdAndUserId(notification.getId(), userId)
                        .isEmpty())
                .count();
    }

    /**
     * Get all notifications sent by admin
     */
    public List<AdminNotificationDto> getNotificationsByAdmin(String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<AdminNotification> notifications = notificationRepository.findByAdminId(admin.getId());
        return notifications.stream()
                .map(notification -> AdminNotificationDto.fromEntity(notification, false))
                .collect(Collectors.toList());
    }

    /**
     * Get all available users for selection (for frontend)
     */
public List<Map<String, Object>> getAllUsers() {
    return userRepository.findAll().stream()
            .map(user -> (Map<String, Object>) new HashMap<String, Object>() {{
                put("id", user.getId());
                put("fullName", user.getFullName());
                put("email", user.getEmail());
                put("role", user.getRole().toString());
            }})
            .collect(Collectors.toList());
}

    /**
     * Delete a notification (admin only)
     */
    public void deleteNotification(Long notificationId, String adminEmail) {
        AdminNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!notification.getAdmin().getId().equals(admin.getId())) {
            throw new RuntimeException("Only the creator can delete this notification");
        }

        notificationRepository.delete(notification);
    }
}
