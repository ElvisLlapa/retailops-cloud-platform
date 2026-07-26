package com.elvisllapa.retailops.inventory.dto.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CreateInventoryRequest(

    @NotNull(message = "Product ID is required")
    UUID productId,

    @NotNull(message = "Store ID is required")
    UUID storeId,

    @PositiveOrZero(message = "Quantity cannot be negative")
    int quantity,

    @PositiveOrZero(message = "Reserved quantity cannot be negative")
    int reservedQuantity
) {
}