package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.AdminUserDto;
import com.commerce.FarmerDirectMarkert.dto.AdminUserUpsertRequest;
import com.commerce.FarmerDirectMarkert.service.AdminUserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserDto>> getAllUsers(Principal principal) {
        return ResponseEntity.ok(adminUserService.getAllUsers(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<AdminUserDto> createUser(
            Principal principal,
            @Valid @RequestBody AdminUserUpsertRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminUserService.createUser(request, principal.getName()));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<AdminUserDto> updateUser(
            Principal principal,
            @PathVariable String userId,
            @Valid @RequestBody AdminUserUpsertRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateUser(userId, request, principal.getName()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(Principal principal, @PathVariable String userId) {
        adminUserService.deleteUser(userId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
