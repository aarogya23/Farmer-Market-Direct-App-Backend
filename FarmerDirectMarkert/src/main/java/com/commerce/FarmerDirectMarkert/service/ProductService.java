package com.commerce.FarmerDirectMarkert.service;

import com.commerce.FarmerDirectMarkert.dto.ProductRequestDto;
import com.commerce.FarmerDirectMarkert.dto.ProductResponseDto;
import com.commerce.FarmerDirectMarkert.model.Category;
import com.commerce.FarmerDirectMarkert.model.Product;
import com.commerce.FarmerDirectMarkert.model.User;
import com.commerce.FarmerDirectMarkert.repository.ProductRepository;
import com.commerce.FarmerDirectMarkert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductResponseDto addProduct(ProductRequestDto request, String userEmail) {
        logger.info("Adding product for user: {}", userEmail);
        
        // Find the user who is adding the product
        User farmer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", userEmail);
                    return new RuntimeException("User not found with email: " + userEmail);
                });

        logger.info("Found user: {} with role: {}", farmer.getFullName(), farmer.getRole());

        // Optionally, check if user is a FARMER
        if (farmer.getRole() != User.Role.FARMER) {
            logger.error("User {} has role {} but only FARMER can add products", userEmail, farmer.getRole());
            throw new RuntimeException("Only farmers can add products. Your role is: " + farmer.getRole());
        }

        // Validate request
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Product name is required");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new RuntimeException("Product price must be greater than 0");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Product quantity must be greater than 0");
        }
        if (request.getCategory() == null) {
            throw new RuntimeException("Product category is required");
        }

        // Create the product entity
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription() != null ? request.getDescription() : "")
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .category(request.getCategory())
                .farmer(farmer)
                .build();

        logger.info("Saving product: {} for farmer: {}", request.getName(), userEmail);
        
        // Save to DB
        Product savedProduct = productRepository.save(product);
        
        logger.info("Product saved successfully with id: {}", savedProduct.getId());

        // Return as DTO
        return mapToDto(savedProduct);
    }

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<ProductResponseDto> getProductsByFarmer(String userEmail) {
        return productRepository.findByFarmerEmail(userEmail)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<ProductResponseDto> searchProducts(String searchTerm, Category category, Double minPrice, Double maxPrice, String sortBy) {
        String order = sortBy == null ? "newest" : sortBy.toLowerCase();
        Sort sort;
        switch (order) {
            case "price_asc":
                sort = Sort.by("price").ascending();
                break;
            case "price_desc":
                sort = Sort.by("price").descending();
                break;
            case "oldest":
                sort = Sort.by("createdAt").ascending();
                break;
            default:
                sort = Sort.by("createdAt").descending();
                break;
        }

        List<Product> products = productRepository.findByFilters(
                category,
                minPrice,
                maxPrice,
                searchTerm == null || searchTerm.isBlank() ? null : searchTerm.trim(),
                sort
        );

        return products.stream()
                .map(this::mapToDto)
                .toList();
    }

    private ProductResponseDto mapToDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(product.getCategory())
                .farmerEmail(product.getFarmer().getEmail())
                .farmerName(product.getFarmer().getFullName())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
