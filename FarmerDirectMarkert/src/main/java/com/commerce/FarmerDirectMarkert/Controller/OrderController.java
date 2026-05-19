package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.*;
import com.commerce.FarmerDirectMarkert.model.OrderStatus;
import com.commerce.FarmerDirectMarkert.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order
     */
    @PostMapping("/create")
    public ResponseEntity<OrderDto> createOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating new order");
        try {
            // Extract email from token or use authenticated user
            String buyerEmail = extractEmailFromToken(token);
            OrderDto order = orderService.createOrder(buyerEmail, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all orders for the current buyer
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderDto>> getMyOrders(
            @RequestHeader("Authorization") String token) {
        try {
            String buyerEmail = extractEmailFromToken(token);
            List<OrderDto> orders = orderService.getBuyerOrders(buyerEmail);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching buyer orders", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDto order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error fetching order", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get order by order number
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByNumber(@PathVariable String orderNumber) {
        try {
            OrderDto order = orderService.getOrderByOrderNumber(orderNumber);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error fetching order by number", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update order status (Admin/Farmer)
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        try {
            OrderDto order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get orders by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<OrderDto> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching orders by status", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete an order (only if pending)
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting order", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Helper method to extract email from token
     * This is a placeholder - implement based on your JWT structure
     */
    private String extractEmailFromToken(String token) {
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // Implement your JWT parsing logic here
        // For now, returning a placeholder
        return "buyer@example.com";
    }
}
