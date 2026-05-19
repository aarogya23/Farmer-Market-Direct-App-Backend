package com.commerce.FarmerDirectMarkert.dto;

import com.commerce.FarmerDirectMarkert.model.Category;
import com.commerce.FarmerDirectMarkert.model.ProductStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceProductDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Category category;
    private String imageUrl;
    private String farmerName;
    private String farmerEmail;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
