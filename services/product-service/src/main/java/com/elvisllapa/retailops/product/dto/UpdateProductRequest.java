package com.elvisllapa.retailops.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(

        @NotBlank
        @Size(max = 50)
        String sku,

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @NotBlank
        @Size(max = 100)
        String brand,

        @NotBlank
        @Size(max = 100)
        String category
) {
}