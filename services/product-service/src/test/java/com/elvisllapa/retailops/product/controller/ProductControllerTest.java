package com.elvisllapa.retailops.product.controller;

import com.elvisllapa.retailops.product.domain.ProductStatus;
import com.elvisllapa.retailops.product.dto.CreateProductRequest;
import com.elvisllapa.retailops.product.dto.ProductResponse;
import com.elvisllapa.retailops.product.exception.ProductNotFoundException;
import com.elvisllapa.retailops.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private UUID productId;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        productResponse = new ProductResponse(
                productId,
                "HEADPHONES-005",
                "Wireless Headphones Pro",
                "Noise-canceling wireless headphones",
                "RetailOps",
                "Electronics",
                ProductStatus.ACTIVE,
                Instant.now(),
                Instant.now(),
                0L
        );
    }

    @Test
    void shouldCreateProductAndReturn201() throws Exception {
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(productResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "HEADPHONES-005",
                                  "name": "Wireless Headphones Pro",
                                  "description": "Noise-canceling wireless headphones",
                                  "brand": "RetailOps",
                                  "category": "Electronics"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.sku").value("HEADPHONES-005"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnProductById() throws Exception {
        when(productService.getProductById(productId))
                .thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Wireless Headphones Pro"))
                .andExpect(jsonPath("$.category")
                        .value("Electronics"));
    }

    @Test
    void shouldReturn400ForInvalidProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "",
                                  "name": "",
                                  "description": "Invalid product",
                                  "brand": "",
                                  "category": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/products"));
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() throws Exception {
        when(productService.getProductById(productId))
                .thenThrow(new ProductNotFoundException(productId));

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/products/" + productId));
    }
}