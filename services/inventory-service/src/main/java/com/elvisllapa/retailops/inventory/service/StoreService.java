package com.elvisllapa.retailops.inventory.service;

import com.elvisllapa.retailops.inventory.dto.store.CreateStoreRequest;
import com.elvisllapa.retailops.inventory.dto.store.StoreResponse;
import com.elvisllapa.retailops.inventory.dto.store.UpdateStoreRequest;

import java.util.List;
import java.util.UUID;

public interface StoreService {

    StoreResponse createStore(CreateStoreRequest request);

    StoreResponse getStoreById(UUID id);

    List<StoreResponse> getAllStores();

    StoreResponse updateStore(UUID id, UpdateStoreRequest request);

    void deleteStore(UUID id);
}