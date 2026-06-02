package com.commerce.FarmerDirectMarkert.repository;

import com.commerce.FarmerDirectMarkert.model.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    @Query("SELECT an FROM AdminNotification an WHERE :userId MEMBER OF an.recipientIds ORDER BY an.createdAt DESC")
    List<AdminNotification> findNotificationsForUser(@Param("userId") String userId);

    @Query("SELECT an FROM AdminNotification an WHERE :role MEMBER OF an.targetRoles ORDER BY an.createdAt DESC")
    List<AdminNotification> findNotificationsByRole(@Param("role") String role);

    @Query("SELECT an FROM AdminNotification an WHERE an.admin.id = :adminId ORDER BY an.createdAt DESC")
    List<AdminNotification> findByAdminId(@Param("adminId") String adminId);

    @Query("SELECT an FROM AdminNotification an WHERE an.createdAt BETWEEN :startDate AND :endDate ORDER BY an.createdAt DESC")
    List<AdminNotification> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
