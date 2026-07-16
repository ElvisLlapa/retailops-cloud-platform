package com.elvisllapa.retailops.product.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID id) {
        super("Product was not found with ID: " + id);
    }

    public ProductNotFoundException(String sku) {
        super("Product was not found with SKU: " + sku);
    }
}