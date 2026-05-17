package com.commerce.FarmerDirectMarkert.dto;

import com.commerce.FarmerDirectMarkert.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private Category category;
    private String farmerEmail;
    private String farmerName;
    private LocalDateTime createdAt;
    private com.commerce.FarmerDirectMarkert.model.ProductStatus status;
    private String imageUrl;
}
