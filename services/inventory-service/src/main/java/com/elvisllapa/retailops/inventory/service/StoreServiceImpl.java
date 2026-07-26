package com.elvisllapa.retailops.inventory.service;

import com.elvisllapa.retailops.inventory.domain.Store;
import com.elvisllapa.retailops.inventory.domain.StoreStatus;
import com.elvisllapa.retailops.inventory.dto.store.CreateStoreRequest;
import com.elvisllapa.retailops.inventory.dto.store.StoreResponse;
import com.elvisllapa.retailops.inventory.dto.store.UpdateStoreRequest;
import com.elvisllapa.retailops.inventory.exception.DuplicateResourceException;
import com.elvisllapa.retailops.inventory.exception.ResourceNotFoundException;
import com.elvisllapa.retailops.inventory.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    public StoreServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public StoreResponse createStore(CreateStoreRequest request) {
        if (storeRepository.existsByStoreNumber(request.storeNumber())) {
            throw new DuplicateResourceException(
                "Store already exists with store number: " + request.storeNumber()
            );
        }

        Store store = new Store();
        store.setStoreNumber(request.storeNumber());
        store.setName(request.name());
        store.setLocation(request.location());
        store.setStatus(StoreStatus.ACTIVE);

        return toResponse(storeRepository.save(store));
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStoreById(UUID id) {
        return toResponse(findStore(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public StoreResponse updateStore(UUID id, UpdateStoreRequest request) {
        Store store = findStore(id);

        store.setName(request.name());
        store.setLocation(request.location());
        store.setStatus(request.status());

        return toResponse(storeRepository.save(store));
    }

    @Override
    public void deleteStore(UUID id) {
        Store store = findStore(id);
        store.setStatus(StoreStatus.INACTIVE);
        storeRepository.save(store);
    }

    private Store findStore(UUID id) {
        return storeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Store not found with ID: " + id
            ));
    }

    private StoreResponse toResponse(Store store) {
        return new StoreResponse(
            store.getId(),
            store.getStoreNumber(),
            store.getName(),
            store.getLocation(),
            store.getStatus(),
            store.getCreatedAt(),
            store.getUpdatedAt(),
            store.getVersion()
        );
    }
}