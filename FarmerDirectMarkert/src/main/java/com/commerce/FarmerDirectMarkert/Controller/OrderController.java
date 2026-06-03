package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.*;
import com.commerce.FarmerDirectMarkert.model.OrderStatus;
import com.commerce.FarmerDirectMarkert.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating new order");
        try {
            String buyerEmail = getCurrentUserEmail(authentication);
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
            Authentication authentication) {
        try {
            String buyerEmail = getCurrentUserEmail(authentication);
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
     * Assign a third-party driver to collect and deliver the order.
     */
    @PostMapping("/{orderId}/assign-driver")
    public ResponseEntity<OrderDto> assignDriver(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody AssignDriverRequest request) {
        try {
            String userEmail = getCurrentUserEmail(authentication);
            OrderDto order = orderService.assignDriverToOrder(userEmail, orderId, request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error assigning driver", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record that the assigned driver has picked up the order and is en route.
     */
    @PatchMapping("/{orderId}/driver/pickup")
    public ResponseEntity<PickupConfirmationDto> markDriverPickup(
            Authentication authentication,
            @PathVariable Long orderId) {
        try {
            String userEmail = getCurrentUserEmail(authentication);
            PickupConfirmationDto order = orderService.markOrderPickedUp(userEmail, orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error marking driver pickup", e);
            return ResponseEntity.badRequest().build();
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
     * Buyer completes a shipped order and uploads PDF receipt for admin verification.
     */
    @PostMapping("/{orderId}/complete-with-receipt")
    public ResponseEntity<OrderDto> completeOrderWithReceipt(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam("file") MultipartFile file) {
        try {
            String buyerEmail = getCurrentUserEmail(authentication);
            OrderDto order = orderService.completeOrderWithReceipt(buyerEmail, orderId, file);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error completing order with receipt", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Admin verifies a submitted receipt.
     */
    @PatchMapping("/{orderId}/verify-receipt")
    public ResponseEntity<OrderDto> verifyReceipt(
            Authentication authentication,
            @PathVariable Long orderId) {
        try {
            String adminEmail = getCurrentUserEmail(authentication);
            OrderDto order = orderService.verifyReceipt(adminEmail, orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Error verifying receipt", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Admin: list delivered orders with receipts pending verification.
     */
    @GetMapping("/receipts/pending-verification")
    public ResponseEntity<List<OrderDto>> getPendingReceiptVerifications(Authentication authentication) {
        try {
            String adminEmail = getCurrentUserEmail(authentication);
            return ResponseEntity.ok(orderService.getOrdersPendingReceiptVerification(adminEmail));
        } catch (Exception e) {
            log.error("Error fetching pending receipt verifications", e);
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

    private String getCurrentUserEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authenticated user not found");
        }
        return authentication.getName();
    }
}
