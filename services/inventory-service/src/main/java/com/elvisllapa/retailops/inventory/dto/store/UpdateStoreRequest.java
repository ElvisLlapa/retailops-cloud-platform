package com.elvisllapa.retailops.inventory.dto.store;

import com.elvisllapa.retailops.inventory.domain.StoreStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStoreRequest(

    @NotBlank(message = "Store name is required")
    @Size(max = 150, message = "Store name must not exceed 150 characters")
    String name,

    @NotBlank(message = "Store location is required")
    @Size(max = 255, message = "Store location must not exceed 255 characters")
    String location,

    @NotNull(message = "Store status is required")
    StoreStatus status
) {
}