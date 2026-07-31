package com.elvisllapa.retailops.inventory.service;

import com.elvisllapa.retailops.inventory.domain.Inventory;
import com.elvisllapa.retailops.inventory.domain.InventoryStatus;
import com.elvisllapa.retailops.inventory.dto.inventory.AdjustInventoryRequest;
import com.elvisllapa.retailops.inventory.dto.inventory.CreateInventoryRequest;
import com.elvisllapa.retailops.inventory.dto.inventory.InventoryResponse;
import com.elvisllapa.retailops.inventory.dto.inventory.StockQuantityRequest;
import com.elvisllapa.retailops.inventory.exception.DuplicateResourceException;
import com.elvisllapa.retailops.inventory.exception.InvalidInventoryOperationException;
import com.elvisllapa.retailops.inventory.exception.ResourceNotFoundException;
import com.elvisllapa.retailops.inventory.repository.InventoryRepository;
import com.elvisllapa.retailops.inventory.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;

    public InventoryServiceImpl(
        InventoryRepository inventoryRepository,
        StoreRepository storeRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        if (!storeRepository.existsById(request.storeId())) {
            throw new ResourceNotFoundException(
                "Store not found with ID: " + request.storeId()
            );
        }

        if (inventoryRepository.existsByProductIdAndStoreId(
            request.productId(),
            request.storeId()
        )) {
            throw new DuplicateResourceException(
                "Inventory already exists for product "
                    + request.productId()
                    + " at store "
                    + request.storeId()
            );
        }

        if (request.reservedQuantity() > request.quantity()) {
            throw new InvalidInventoryOperationException(
                "Reserved quantity cannot exceed total quantity"
            );
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(request.productId());
        inventory.setStoreId(request.storeId());
        inventory.setQuantity(request.quantity());
        inventory.setReservedQuantity(request.reservedQuantity());
        inventory.setStatus(InventoryStatus.IN_STOCK);

        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(UUID id) {
        return toResponse(findInventory(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByStore(UUID storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException(
                "Store not found with ID: " + storeId
            );
        }

        return inventoryRepository.findByStoreId(storeId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByProduct(UUID productId) {
        return inventoryRepository.findByProductId(productId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public InventoryResponse adjustInventory(
        UUID id,
        AdjustInventoryRequest request
    ) {
        Inventory inventory = findInventory(id);
        validateInventoryIsActive(inventory);
        int newQuantity = inventory.getQuantity() + request.adjustment();

        if (newQuantity < 0) {
            throw new InvalidInventoryOperationException(
                "Inventory quantity cannot be reduced below zero"
            );
        }

        if (newQuantity < inventory.getReservedQuantity()) {
            throw new InvalidInventoryOperationException(
                "Inventory quantity cannot be less than reserved quantity"
            );
        }

        inventory.setQuantity(newQuantity);

        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryResponse reserveStock(
        UUID id,
        StockQuantityRequest request
    ) {
        Inventory inventory = findInventory(id);
        validateInventoryIsActive(inventory);
        int availableQuantity =
            inventory.getQuantity() - inventory.getReservedQuantity();

        if (request.quantity() > availableQuantity) {
            throw new InvalidInventoryOperationException(
                "Not enough available inventory to reserve "
                    + request.quantity()
                    + " units"
            );
        }

        inventory.setReservedQuantity(
            inventory.getReservedQuantity() + request.quantity()
        );

        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryResponse releaseStock(
        UUID id,
        StockQuantityRequest request
    ) {
        Inventory inventory = findInventory(id);
        validateInventoryIsActive(inventory);

        if (request.quantity() > inventory.getReservedQuantity()) {
            throw new InvalidInventoryOperationException(
                "Cannot release more inventory than is currently reserved"
            );
        }

        inventory.setReservedQuantity(
            inventory.getReservedQuantity() - request.quantity()
        );

        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    public void deleteInventory(UUID id) {
        Inventory inventory = findInventory(id);
        inventory.setStatus(InventoryStatus.DISCONTINUED);
        inventoryRepository.save(inventory);
    }

    private Inventory findInventory(UUID id) {
        return inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventory not found with ID: " + id
            ));
    }

    private void validateInventoryIsActive(Inventory inventory) {
        if (inventory.getStatus() == InventoryStatus.DISCONTINUED) {
            throw new InvalidInventoryOperationException(
                "Cannot modify discontinued inventory"
            );
        }
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
            inventory.getId(),
            inventory.getProductId(),
            inventory.getStoreId(),
            inventory.getQuantity(),
            inventory.getReservedQuantity(),
            inventory.getQuantity() - inventory.getReservedQuantity(),
            inventory.getStatus(),
            inventory.getCreatedAt(),
            inventory.getUpdatedAt(),
            inventory.getVersion()
        );
    }
}