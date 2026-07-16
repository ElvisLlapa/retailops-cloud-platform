package com.elvisllapa.retailops.product.dto;

import com.elvisllapa.retailops.product.domain.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(

        @NotNull
        ProductStatus status
) {
}