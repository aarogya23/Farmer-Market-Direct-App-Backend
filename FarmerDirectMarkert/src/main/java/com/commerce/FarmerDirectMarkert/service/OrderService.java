package com.commerce.FarmerDirectMarkert.service;

import com.commerce.FarmerDirectMarkert.dto.*;
import com.commerce.FarmerDirectMarkert.model.*;
import com.commerce.FarmerDirectMarkert.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private static final String RECEIPT_UPLOAD_DIR = "uploads/receipts/";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final ReceiptNotificationService receiptNotificationService;

    /**
     * Create a new order with multiple items
     */
    public OrderDto createOrder(String buyerEmail, CreateOrderRequest request) {
        log.info("Creating order for buyer: {}", buyerEmail);

        // Get buyer
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        if (buyer.getRole() != User.Role.BUYER && buyer.getRole() != User.Role.FARMER) {
            throw new RuntimeException("Only buyers can create orders");
        }

        // Create order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setBuyer(buyer);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setNotes(request.getNotes());
        if (request.getExpectedDeliveryDate() != null) {
            order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        }

        // Add items
        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0;

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));

            // Check if product is available
            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient quantity for product: " + product.getName());
            }

            // Create order item
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(product.getPrice());

            orderItems.add(item);
            totalPrice += product.getPrice() * itemRequest.getQuantity();

            // Reduce product quantity
            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setItems(orderItems);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());

        return convertToDto(savedOrder);
    }

    /**
     * Get all orders for a buyer
     */
    public List<OrderDto> getBuyerOrders(String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        return orderRepository.findByBuyerId(buyer.getId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get order by ID
     */
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToDto(order);
    }

    /**
     * Get order by order number
     */
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToDto(order);
    }

    /**
     * Update order status
     */
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        log.info("Updating order {} status from {} to {}", orderId, order.getStatus(), newStatus);

        // Validate status transitions
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }

        if (newStatus == OrderStatus.SHIPPED && order.getDriver() == null) {
            throw new RuntimeException("Driver must be assigned before shipping");
        }

        // If cancelling, restore product quantities
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return convertToDto(updatedOrder);
    }

    /**
     * Assign a third-party driver to an order so they can collect and deliver items.
     */
    public OrderDto assignDriverToOrder(String userEmail, Long orderId, AssignDriverRequest request) {
        User user = requireAdminOrFarmer(userEmail);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot assign driver to cancelled or delivered orders");
        }

        Driver driver = driverRepository.findByPhone(request.getDriverPhone())
                .orElseGet(() -> driverRepository.save(Driver.builder()
                        .name(request.getDriverName())
                        .phone(request.getDriverPhone())
                        .company(request.getDriverCompany())
                        .build()));

        order.setDriver(driver);
        order.setDriverPickupLocation(request.getPickupLocation());
        order.setDriverAssignedAt(LocalDateTime.now());

        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.PROCESSING);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Driver {} assigned to order {} by {}", request.getDriverName(), orderId, user.getEmail());
        return convertToDto(savedOrder);
    }

    /**
     * Mark the order as picked up and in transit.
     */
    public PickupConfirmationDto markOrderPickedUp(String userEmail, Long orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getDriver() == null) {
            throw new RuntimeException("A driver must be assigned before pickup can be recorded");
        }

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot pick up a cancelled or already delivered order");
        }

        boolean isAdminOrFarmer = user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.FARMER;
        boolean isAssignedDriver = user.getRole() == User.Role.DRIVER
                && order.getDriver().getUserId().equals(user.getId());

        if (!isAdminOrFarmer && !isAssignedDriver) {
            throw new RuntimeException("Only admin, farmer, or the assigned driver can mark pickup");
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setDriverPickupAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} marked as picked up by driver {}", orderId,
                savedOrder.getDriver() != null ? savedOrder.getDriver().getName() : "<unknown>");
        return convertToPickupConfirmationDto(savedOrder);
    }

    /**
     * Get all orders by status
     */
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Buyer marks order complete (SHIPPED -> DELIVERED) and uploads PDF receipt for admin verification.
     */
    public OrderDto completeOrderWithReceipt(String buyerEmail, Long orderId, MultipartFile receiptFile) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new RuntimeException("You can only complete your own orders");
        }

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Order can only be completed when status is SHIPPED");
        }

        if (receiptFile == null || receiptFile.isEmpty()) {
            throw new RuntimeException("Receipt PDF is required");
        }

        String originalName = receiptFile.getOriginalFilename();
        String contentType = receiptFile.getContentType();
        boolean isPdf = (contentType != null && contentType.contains("pdf"))
                || (originalName != null && originalName.toLowerCase().endsWith(".pdf"));
        if (!isPdf) {
            throw new RuntimeException("Receipt must be a PDF file");
        }

        try {
            Path directory = Paths.get(RECEIPT_UPLOAD_DIR);
            Files.createDirectories(directory);

            String filename = "receipt-" + order.getOrderNumber() + "-" + UUID.randomUUID() + ".pdf";
            Path target = directory.resolve(filename);
            Files.write(target, receiptFile.getBytes());

            order.setReceiptFileName(filename);
            order.setReceiptSubmittedAt(LocalDateTime.now());
            order.setReceiptVerified(false);
            order.setCompletedAt(LocalDateTime.now());
            order.setStatus(OrderStatus.DELIVERED);

            Order saved = orderRepository.save(order);
            receiptNotificationService.notifyAdminOrderReceipt(buyerEmail, saved);
            log.info("Order {} completed by buyer with receipt {}", orderId, filename);
            return convertToDto(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save receipt PDF", e);
        }
    }

    /**
     * Admin verifies a submitted receipt.
     */
    public OrderDto verifyReceipt(String adminEmail, Long orderId) {
        requireAdmin(adminEmail);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getReceiptFileName() == null) {
            throw new RuntimeException("No receipt uploaded for this order");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Receipt can only be verified for delivered orders");
        }

        order.setReceiptVerified(true);
        return convertToDto(orderRepository.save(order));
    }

    /**
     * Orders with receipts waiting for admin verification.
     */
    public List<OrderDto> getOrdersPendingReceiptVerification(String adminEmail) {
        requireAdmin(adminEmail);
        return orderRepository
                .findByStatusAndReceiptFileNameIsNotNullAndReceiptVerifiedFalse(OrderStatus.DELIVERED)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private User requireAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Admin access required");
        }
        return user;
    }

    private User requireAdminOrFarmer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.FARMER) {
            throw new RuntimeException("Admin or farmer access required");
        }
        return user;
    }

    /**
     * Convert Order to lightweight PickupConfirmationDto
     */
    private PickupConfirmationDto convertToPickupConfirmationDto(Order order) {
        List<PickupConfirmationDto.ProductItemDto> items = order.getItems().stream()
                .map(item -> PickupConfirmationDto.ProductItemDto.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .imageUrl(item.getProduct().getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return PickupConfirmationDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerName(order.getBuyer().getFullName())
                .buyerEmail(order.getBuyer().getEmail())
                .items(items)
                .deliveryAddress(order.getDeliveryAddress())
                .driverName(order.getDriver() != null ? order.getDriver().getName() : null)
                .driverPhone(order.getDriver() != null ? order.getDriver().getPhone() : null)
                .driverCompany(order.getDriver() != null ? order.getDriver().getCompany() : null)
                .status(order.getStatus().toString())
                .build();
    }

    /**
     * Delete an order (only if pending)
     */
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Can only delete pending orders");
        }

        // Restore product quantities
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        orderRepository.delete(order);
        log.info("Order deleted: {}", orderId);
    }

    /**
     * Convert Order entity to DTO
     */
    private OrderDto convertToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .imageUrl(item.getProduct().getImageUrl())
                        .build())
                .collect(Collectors.toList());

        String receiptUrl = null;
        if (order.getReceiptFileName() != null) {
            receiptUrl = "/uploads/receipts/" + order.getReceiptFileName();
        }

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerName(order.getBuyer().getFullName())
                .buyerEmail(order.getBuyer().getEmail())
                .items(itemDtos)
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .deliveryAddress(order.getDeliveryAddress())
                .notes(order.getNotes())
                .driverId(order.getDriver() != null ? order.getDriver().getId() : null)
                .driverName(order.getDriver() != null ? order.getDriver().getName() : null)
                .driverPhone(order.getDriver() != null ? order.getDriver().getPhone() : null)
                .driverCompany(order.getDriver() != null ? order.getDriver().getCompany() : null)
                .driverPickupLocation(order.getDriverPickupLocation())
                .driverAssignedAt(order.getDriverAssignedAt())
                .driverPickupAt(order.getDriverPickupAt())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .receiptUrl(receiptUrl)
                .receiptSubmittedAt(order.getReceiptSubmittedAt())
                .receiptVerified(order.getReceiptVerified())
                .completedAt(order.getCompletedAt())
                .build();
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
    }
}
