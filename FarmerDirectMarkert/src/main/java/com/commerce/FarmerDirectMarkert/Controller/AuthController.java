package com.commerce.FarmerDirectMarkert.Controller;


import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.commerce.FarmerDirectMarkert.dto.AuthResponse;
import com.commerce.FarmerDirectMarkert.dto.LoginRequest;
import com.commerce.FarmerDirectMarkert.dto.SignupRequest;
import com.commerce.FarmerDirectMarkert.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/signup
     * Register a new user (FARMER, BUYER, or ADMIN)
     *
     * Possible errors:
     *  - 400 if any field is blank/null/invalid format  → caught by @Valid + GlobalExceptionHandler
     *  - 409 if email is already registered             → throws DuplicateEmailException
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody com.commerce.FarmerDirectMarkert.dto.GoogleLoginRequest request) {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }
        AuthResponse response = authService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}