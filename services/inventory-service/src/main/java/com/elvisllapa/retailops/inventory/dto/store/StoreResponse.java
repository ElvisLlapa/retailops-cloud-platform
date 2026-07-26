package com.elvisllapa.retailops.inventory.dto.store;

import com.elvisllapa.retailops.inventory.domain.StoreStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record StoreResponse(
    UUID id,
    String storeNumber,
    String name,
    String location,
    StoreStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long version
) {
}