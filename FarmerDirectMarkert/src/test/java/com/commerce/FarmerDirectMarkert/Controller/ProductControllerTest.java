package com.commerce.FarmerDirectMarkert.Controller;

import com.commerce.FarmerDirectMarkert.dto.ProductRequestDto;
import com.commerce.FarmerDirectMarkert.dto.ProductResponseDto;
import com.commerce.FarmerDirectMarkert.model.Category;
import com.commerce.FarmerDirectMarkert.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productService)).build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProductsReturnsFilteredResults() throws Exception {
        ProductResponseDto product = sampleResponse();
        when(productService.searchProducts("apple", Category.FRUIT, 10.0, 50.0, "price_asc"))
                .thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/test")
                        .param("q", "apple")
                        .param("category", "fruit")
                        .param("minPrice", "10")
                        .param("maxPrice", "50")
                        .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fresh Apples"))
                .andExpect(jsonPath("$[0].category").value("FRUIT"));

        verify(productService).searchProducts("apple", Category.FRUIT, 10.0, 50.0, "price_asc");
    }

    @Test
    void getProductsReturnsBadRequestForInvalidCategory() throws Exception {
        mockMvc.perform(get("/api/products/test")
                        .param("category", "snacks"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProductsReturnsProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/products/allProducts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].farmerEmail").value("farmer@example.com"));

        verify(productService).getAllProducts();
    }

    @Test
    void getMyProductsReturnsAuthenticatedUsersProducts() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("farmer@example.com", "password")
        );
        when(productService.getProductsByFarmer("farmer@example.com")).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/products/my-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].farmerName").value("Test Farmer"));

        verify(productService).getProductsByFarmer("farmer@example.com");
    }

    @Test
    void addProductCreatesProductForAuthenticatedUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("farmer@example.com", "password")
        );

        ProductRequestDto request = ProductRequestDto.builder()
                .name("Fresh Apples")
                .description("Crisp and sweet")
                .price(25.0)
                .quantity(10)
                .category(Category.FRUIT)
                .build();

        when(productService.addProduct(eq(request), eq("farmer@example.com"))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fresh Apples"))
                .andExpect(jsonPath("$.price").value(25.0));

        verify(productService).addProduct(eq(request), eq("farmer@example.com"));
    }

    private ProductResponseDto sampleResponse() {
        return ProductResponseDto.builder()
                .id(1L)
                .name("Fresh Apples")
                .description("Crisp and sweet")
                .price(25.0)
                .quantity(10)
                .category(Category.FRUIT)
                .farmerEmail("farmer@example.com")
                .farmerName("Test Farmer")
                .createdAt(LocalDateTime.of(2026, 5, 1, 12, 0))
                .build();
    }
}
