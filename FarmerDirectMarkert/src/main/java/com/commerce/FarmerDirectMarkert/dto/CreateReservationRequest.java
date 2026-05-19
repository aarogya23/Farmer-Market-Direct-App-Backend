package com.commerce.FarmerDirectMarkert.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Reserved quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer reservedQuantity;
    
    @NotNull(message = "Reserved from time is required")
    private LocalDateTime reservedFromTime;
    
    @NotNull(message = "Reserved until time is required")
    private LocalDateTime reservedUntilTime;
    
    private String notes;
}
