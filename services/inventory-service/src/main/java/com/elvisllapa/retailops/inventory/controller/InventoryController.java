package com.elvisllapa.retailops.inventory.controller;

import com.elvisllapa.retailops.inventory.dto.inventory.AdjustInventoryRequest;
import com.elvisllapa.retailops.inventory.dto.inventory.CreateInventoryRequest;
import com.elvisllapa.retailops.inventory.dto.inventory.InventoryResponse;
import com.elvisllapa.retailops.inventory.dto.inventory.StockQuantityRequest;
import com.elvisllapa.retailops.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
        @Valid @RequestBody CreateInventoryRequest request
    ) {
        InventoryResponse response =
            inventoryService.createInventory(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
            inventoryService.getInventoryById(id)
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<InventoryResponse>> getInventoryByStore(
        @PathVariable UUID storeId
    ) {
        return ResponseEntity.ok(
            inventoryService.getInventoryByStore(storeId)
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryResponse>> getInventoryByProduct(
        @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(
            inventoryService.getInventoryByProduct(productId)
        );
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<InventoryResponse> adjustInventory(
        @PathVariable UUID id,
        @Valid @RequestBody AdjustInventoryRequest request
    ) {
        return ResponseEntity.ok(
            inventoryService.adjustInventory(id, request)
        );
    }

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(
        @PathVariable UUID id,
        @Valid @RequestBody StockQuantityRequest request
    ) {
        return ResponseEntity.ok(
            inventoryService.reserveStock(id, request)
        );
    }

    @PatchMapping("/{id}/release")
    public ResponseEntity<InventoryResponse> releaseStock(
        @PathVariable UUID id,
        @Valid @RequestBody StockQuantityRequest request
    ) {
        return ResponseEntity.ok(
            inventoryService.releaseStock(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(
        @PathVariable UUID id
    ) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
}