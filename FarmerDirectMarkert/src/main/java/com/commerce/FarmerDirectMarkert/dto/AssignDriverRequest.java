package com.commerce.FarmerDirectMarkert.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverRequest {

    @NotBlank(message = "Driver name is required")
    private String driverName;

    @NotBlank(message = "Driver phone is required")
    private String driverPhone;

    private String driverCompany;

    private String pickupLocation;
}
