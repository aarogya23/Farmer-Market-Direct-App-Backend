package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.AdminNotificationDto;
import com.commerce.FarmerDirectMarkert.dto.SendNotificationRequest;
import com.commerce.FarmerDirectMarkert.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService notificationService;

    /**
     * Send a notification to selected users or roles
     * POST /api/admin/notifications/send
     */
    @PostMapping("/send")
    public ResponseEntity<AdminNotificationDto> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication.getName();

        AdminNotificationDto notification = notificationService.sendNotification(request, adminEmail);
        return ResponseEntity.ok(notification);
    }

    /**
     * Get all notifications for the current user
     * GET /api/admin/notifications/my-notifications
     */
    @GetMapping("/my-notifications")
    public ResponseEntity<List<AdminNotificationDto>> getMyNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // This will be user email, need to convert to ID

        // Note: This will need modification if storing by email vs ID
        List<AdminNotificationDto> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark notification as read
     * POST /api/admin/notifications/{notificationId}/read
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Note: Need to convert email to user ID or modify service
        notificationService.markNotificationAsRead(notificationId, userEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Get unread notification count
     * GET /api/admin/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Integer count = notificationService.getUnreadNotificationCount(userEmail);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Get all users for notification selection
     * GET /api/admin/notifications/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = notificationService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get notifications sent by the current admin
     * GET /api/admin/notifications/my-sent
     */
    @GetMapping("/my-sent")
    public ResponseEntity<List<AdminNotificationDto>> getMysentNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication.getName();

        List<AdminNotificationDto> notifications = notificationService.getNotificationsByAdmin(adminEmail);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Delete a notification (admin only)
     * DELETE /api/admin/notifications/{notificationId}
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication.getName();

        notificationService.deleteNotification(notificationId, adminEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Get notifications by role
     * GET /api/admin/notifications/by-role/{role}
     */
    @GetMapping("/by-role/{role}")
    public ResponseEntity<List<AdminNotificationDto>> getNotificationsByRole(@PathVariable String role) {
        List<AdminNotificationDto> notifications = notificationService.getNotificationsByRole(role);
        return ResponseEntity.ok(notifications);
    }
}
