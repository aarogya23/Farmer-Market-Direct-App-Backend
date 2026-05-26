package com.commerce.FarmerDirectMarkert.service;

import com.commerce.FarmerDirectMarkert.dto.AdminUserDto;
import com.commerce.FarmerDirectMarkert.dto.AdminUserUpsertRequest;
import com.commerce.FarmerDirectMarkert.model.User;
import com.commerce.FarmerDirectMarkert.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AdminUserDto> getAllUsers(String adminEmail) {
        User admin = requireAdmin(adminEmail);
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .map(AdminUserDto::fromUser)
                .toList();
    }

    public AdminUserDto createUser(AdminUserUpsertRequest request, String adminEmail) {
        requireAdmin(adminEmail);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required when creating a user");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        return AdminUserDto.fromUser(userRepository.save(user));
    }

    public AdminUserDto updateUser(String userId, AdminUserUpsertRequest request, String adminEmail) {
        User admin = requireAdmin(adminEmail);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        userRepository.findByEmail(normalizedEmail)
                .filter(existingUser -> !existingUser.getId().equals(userId))
                .ifPresent(existingUser -> {
                    throw new RuntimeException("Email already registered: " + normalizedEmail);
                });

        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (admin.getId().equals(user.getId()) && request.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Admins cannot remove their own admin role");
        }

        return AdminUserDto.fromUser(userRepository.save(user));
    }

    public void deleteUser(String userId, String adminEmail) {
        User admin = requireAdmin(adminEmail);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getId().equals(user.getId())) {
            throw new RuntimeException("Admins cannot delete their own account");
        }

        userRepository.delete(user);
    }

    private User requireAdmin(String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admins can manage users");
        }

        return admin;
    }
}
