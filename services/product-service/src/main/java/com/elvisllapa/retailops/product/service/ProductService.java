package com.elvisllapa.retailops.product.service;

import com.elvisllapa.retailops.product.domain.ProductStatus;
import com.elvisllapa.retailops.product.dto.CreateProductRequest;
import com.elvisllapa.retailops.product.dto.ProductResponse;
import com.elvisllapa.retailops.product.dto.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    Page<ProductResponse> getProducts(
            String category,
            String brand,
            ProductStatus status,
            String search,
            Pageable pageable
    );

    ProductResponse getProductById(UUID id);

    ProductResponse getProductBySku(String sku);

    ProductResponse updateProduct(UUID id, UpdateProductRequest request);

    ProductResponse updateProductStatus(UUID id, ProductStatus status);

    void discontinueProduct(UUID id);
}