package com.elvisllapa.retailops.product.controller;

import com.elvisllapa.retailops.product.domain.ProductStatus;
import com.elvisllapa.retailops.product.dto.CreateProductRequest;
import com.elvisllapa.retailops.product.dto.ProductResponse;
import com.elvisllapa.retailops.product.dto.UpdateProductRequest;
import com.elvisllapa.retailops.product.dto.UpdateProductStatusRequest;
import com.elvisllapa.retailops.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public Page<ProductResponse> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return productService.getProducts(
                category,
                brand,
                status,
                search,
                pageable
        );
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping("/sku/{sku}")
    public ProductResponse getProductBySku(@PathVariable String sku) {
        return productService.getProductBySku(sku);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @PatchMapping("/{id}/status")
    public ProductResponse updateProductStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        return productService.updateProductStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> discontinueProduct(@PathVariable UUID id) {
        productService.discontinueProduct(id);
        return ResponseEntity.noContent().build();
    }
}