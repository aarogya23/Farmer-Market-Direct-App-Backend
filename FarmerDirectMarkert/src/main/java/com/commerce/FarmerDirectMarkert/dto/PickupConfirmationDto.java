package com.commerce.FarmerDirectMarkert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickupConfirmationDto {

    private Long orderId;
    private String orderNumber;

    // Buyer info
    private String buyerName;
    private String buyerEmail;

    // Products
    private List<ProductItemDto> items;

    // Destination
    private String deliveryAddress;

    // Driver info
    private String driverName;
    private String driverPhone;
    private String driverCompany;

    // Status
    private String status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItemDto {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double price;
        private String imageUrl;
    }
}
