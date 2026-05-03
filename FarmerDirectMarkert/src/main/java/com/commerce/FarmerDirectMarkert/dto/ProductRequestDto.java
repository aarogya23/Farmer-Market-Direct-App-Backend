package com.commerce.FarmerDirectMarkert.dto;

import com.commerce.FarmerDirectMarkert.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private Category category;
}
