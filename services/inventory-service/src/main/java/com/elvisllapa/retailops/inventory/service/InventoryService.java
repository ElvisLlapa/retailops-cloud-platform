package com.elvisllapa.retailops.inventory.service;

import com.elvisllapa.retailops.inventory.dto.inventory.AdjustInventoryRequest;
import com.elvisllapa.retailops.inventory.dto.inventory.CreateInventoryRequest;
import com.elvisllapa.retailops.inventory.dto.inventory.InventoryResponse;
import com.elvisllapa.retailops.inventory.dto.inventory.StockQuantityRequest;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    InventoryResponse createInventory(CreateInventoryRequest request);

    InventoryResponse getInventoryById(UUID id);

    List<InventoryResponse> getInventoryByStore(UUID storeId);

    List<InventoryResponse> getInventoryByProduct(UUID productId);

    InventoryResponse adjustInventory(
        UUID id,
        AdjustInventoryRequest request
    );

    InventoryResponse reserveStock(
        UUID id,
        StockQuantityRequest request
    );

    InventoryResponse releaseStock(
        UUID id,
        StockQuantityRequest request
    );

    void deleteInventory(UUID id);
}