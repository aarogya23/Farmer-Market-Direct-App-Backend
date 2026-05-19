package com.commerce.FarmerDirectMarkert.dto;

import com.commerce.FarmerDirectMarkert.model.OrderStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private String orderNumber;
    private String buyerName;
    private String buyerEmail;
    private List<OrderItemDto> items;
    private OrderStatus status;
    private Double totalPrice;
    private String deliveryAddress;
    private String notes;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
