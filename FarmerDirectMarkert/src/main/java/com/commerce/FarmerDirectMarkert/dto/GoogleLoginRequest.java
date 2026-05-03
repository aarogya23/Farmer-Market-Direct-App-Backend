package com.commerce.FarmerDirectMarkert.dto;

import com.commerce.FarmerDirectMarkert.model.User.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginRequest {

    @NotBlank(message = "ID Token is required")
    private String idToken;

    // Optional: role for first-time sign-ups (defaults to BUYER if not provided)
    private Role role;
}
