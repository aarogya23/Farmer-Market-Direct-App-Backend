package com.commerce.FarmerDirectMarkert.repository;

import com.commerce.FarmerDirectMarkert.model.Order;
import com.commerce.FarmerDirectMarkert.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByBuyerId(String buyerId);
    List<Order> findByBuyerIdAndStatus(String buyerId, OrderStatus status);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
