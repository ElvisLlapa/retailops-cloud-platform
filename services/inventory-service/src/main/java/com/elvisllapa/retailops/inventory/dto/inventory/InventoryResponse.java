package com.elvisllapa.retailops.inventory.dto.inventory;

import com.elvisllapa.retailops.inventory.domain.InventoryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(
    UUID id,
    UUID productId,
    UUID storeId,
    int quantity,
    int reservedQuantity,
    int availableQuantity,
    InventoryStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long version
) {
}