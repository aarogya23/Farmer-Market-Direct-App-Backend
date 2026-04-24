package com.commerce.FarmerDirectMarkert.dto;

import com.commerce.FarmerDirectMarkert.model.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "Full name is reuired")
    private String fullName;


    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is reuired")
    @Size(min = 6, message="password must be at least 6 charaters")
    private String password;

    @NotNull(message = "Role is required (Farmer, Buyer, or Admin)")
    private User.Role role;

}
