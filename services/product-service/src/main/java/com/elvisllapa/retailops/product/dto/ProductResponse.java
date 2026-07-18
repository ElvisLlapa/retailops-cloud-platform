package com.elvisllapa.retailops.product.dto;

import com.elvisllapa.retailops.product.domain.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String brand,
        String category,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}