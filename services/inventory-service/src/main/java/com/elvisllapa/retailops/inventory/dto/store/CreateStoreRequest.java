package com.elvisllapa.retailops.inventory.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStoreRequest(

    @NotBlank(message = "Store number is required")
    @Size(max = 50, message = "Store number must not exceed 50 characters")
    String storeNumber,

    @NotBlank(message = "Store name is required")
    @Size(max = 150, message = "Store name must not exceed 150 characters")
    String name,

    @NotBlank(message = "Store location is required")
    @Size(max = 255, message = "Store location must not exceed 255 characters")
    String location
) {
}