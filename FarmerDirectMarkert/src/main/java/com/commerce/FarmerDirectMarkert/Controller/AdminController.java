package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.ProductResponseDto;
import com.commerce.FarmerDirectMarkert.model.ProductStatus;
import com.commerce.FarmerDirectMarkert.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;

    @GetMapping("/pending")
    public ResponseEntity<List<ProductResponseDto>> getPendingProducts() {
        return ResponseEntity.ok(productService.getProductsByStatus(ProductStatus.PENDING));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponseDto> updateProductStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload
    ) {
        String statusStr = payload.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().build();
        }

        ProductStatus status;
        try {
            status = ProductStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        ProductResponseDto updatedProduct = productService.updateProductStatus(id, status, currentPrincipalName);
        return ResponseEntity.ok(updatedProduct);
    }
}
