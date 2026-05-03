package com.commerce.FarmerDirectMarkert.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.commerce.FarmerDirectMarkert.dto.AuthResponse;
import com.commerce.FarmerDirectMarkert.dto.LoginRequest;
import com.commerce.FarmerDirectMarkert.dto.SignupRequest;
import com.commerce.FarmerDirectMarkert.model.User;
import com.commerce.FarmerDirectMarkert.repository.UserRepository;

import com.commerce.FarmerDirectMarkert.dto.GoogleLoginRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifierService googleTokenVerifierService;
 
    // ── Signup ────────────────────────────────────────────────────────────────
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }
 
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
 
        User savedUser = userRepository.save(user);
 
        // Use email as the UserDetails principal
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        savedUser.getEmail(),
                        savedUser.getPassword(),
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + savedUser.getRole().name()))
                );
 
        String token = jwtService.generateToken(userDetails);
 
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }
 
    // ── Login ─────────────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {
        // This throws AuthenticationException if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
 
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()))
                );
 
        String token = jwtService.generateToken(userDetails);
 
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // Add this to AuthService.java after the login method
    public AuthResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    // ── Google Login ──────────────────────────────────────────────────────────
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        // 1. Verify token with Google
        GoogleIdToken.Payload payload = googleTokenVerifierService.verifyToken(request.getIdToken());

        // 2. Extract user information
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // 3. Find existing user by email
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // New user, register them automatically
            User.Role role = request.getRole() != null ? request.getRole() : User.Role.BUYER;
            user = User.builder()
                    .fullName(name != null ? name : "Google User")
                    .email(email)
                    // Generate a random, long password as they will use Google to sign in
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(role)
                    .build();
            user = userRepository.save(user);
        }

        // 4. Generate standard JWT
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()))
                );

        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}