package com.elvisllapa.retailops.inventory.dto.inventory;

import jakarta.validation.constraints.NotNull;

public record AdjustInventoryRequest(

    @NotNull(message = "Adjustment amount is required")
    Integer adjustment
) {
}