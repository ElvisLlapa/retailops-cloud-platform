package com.elvisllapa.retailops.inventory.controller;

import com.elvisllapa.retailops.inventory.dto.store.CreateStoreRequest;
import com.elvisllapa.retailops.inventory.dto.store.StoreResponse;
import com.elvisllapa.retailops.inventory.dto.store.UpdateStoreRequest;
import com.elvisllapa.retailops.inventory.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
        @Valid @RequestBody CreateStoreRequest request
    ) {
        StoreResponse response = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getStoreById(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreResponse> updateStore(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateStoreRequest request
    ) {
        return ResponseEntity.ok(storeService.updateStore(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}