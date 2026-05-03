package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.ProductRequestDto;
import com.commerce.FarmerDirectMarkert.dto.ProductResponseDto;
import com.commerce.FarmerDirectMarkert.model.Category;
import com.commerce.FarmerDirectMarkert.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDto> addProduct(@RequestBody ProductRequestDto productRequestDto) {
        // Extract authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName(); // this gets the email from UserDetails

        ProductResponseDto response = productService.addProduct(productRequestDto, currentPrincipalName);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/test")
    public ResponseEntity<List<ProductResponseDto>> getProducts(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "category", required = false) String categoryName,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort
    ) {
        Category category = null;
        if (categoryName != null && !categoryName.isBlank()) {
            try {
                category = Category.valueOf(categoryName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok(productService.searchProducts(q, category, minPrice, maxPrice, sort));
    }

    @GetMapping("/allProducts")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/my-products")
    public ResponseEntity<List<ProductResponseDto>> getMyProducts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        return ResponseEntity.ok(productService.getProductsByFarmer(currentPrincipalName));
    }
}
