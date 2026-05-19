package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.MarketplaceProductDto;
import com.commerce.FarmerDirectMarkert.model.Category;
import com.commerce.FarmerDirectMarkert.service.MarketplaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
@Slf4j
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    /**
     * Get all available products in the marketplace
     */
    @GetMapping("/products")
    public ResponseEntity<List<MarketplaceProductDto>> getAllProducts() {
        log.info("Fetching all marketplace products");
        try {
            List<MarketplaceProductDto> products = marketplaceService.getAllMarketplaceProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching marketplace products", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Search products by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<MarketplaceProductDto>> searchProducts(
            @RequestParam String query) {
        log.info("Searching products with query: {}", query);
        try {
            List<MarketplaceProductDto> products = marketplaceService.searchProductsByName(query);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error searching products", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<MarketplaceProductDto>> getProductsByCategory(
            @PathVariable Category category) {
        log.info("Fetching products for category: {}", category);
        try {
            List<MarketplaceProductDto> products = marketplaceService.getProductsByCategory(category);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching products by category", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get products by farmer
     */
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<MarketplaceProductDto>> getProductsByFarmer(
            @PathVariable String farmerId) {
        log.info("Fetching products for farmer: {}", farmerId);
        try {
            List<MarketplaceProductDto> products = marketplaceService.getProductsByFarmer(farmerId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching products by farmer", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get popular products
     */
    @GetMapping("/popular")
    public ResponseEntity<List<MarketplaceProductDto>> getPopularProducts() {
        log.info("Fetching popular products");
        try {
            List<MarketplaceProductDto> products = marketplaceService.getPopularProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching popular products", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get newly added products
     */
    @GetMapping("/new")
    public ResponseEntity<List<MarketplaceProductDto>> getNewProducts() {
        log.info("Fetching new products");
        try {
            List<MarketplaceProductDto> products = marketplaceService.getNewProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error fetching new products", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get product details
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<MarketplaceProductDto> getProductDetails(@PathVariable Long productId) {
        log.info("Fetching product details for: {}", productId);
        try {
            MarketplaceProductDto product = marketplaceService.getProductDetails(productId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error fetching product details", e);
            return ResponseEntity.notFound().build();
        }
    }
}
