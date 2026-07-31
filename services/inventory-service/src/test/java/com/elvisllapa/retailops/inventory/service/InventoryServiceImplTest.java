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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StoreRepository storeRepository;

    private InventoryServiceImpl inventoryService;

    private UUID inventoryId;
    private UUID productId;
    private UUID storeId;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(
            inventoryRepository,
            storeRepository
        );

        inventoryId = UUID.randomUUID();
        productId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setStoreId(storeId);
        inventory.setQuantity(100);
        inventory.setReservedQuantity(20);
        inventory.setStatus(InventoryStatus.IN_STOCK);
    }

    @Test
    void createInventoryCreatesInventorySuccessfully() {
        CreateInventoryRequest request = new CreateInventoryRequest(
            productId,
            storeId,
            100,
            20
        );

        when(storeRepository.existsById(storeId)).thenReturn(true);
        when(inventoryRepository.existsByProductIdAndStoreId(
            productId,
            storeId
        )).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response =
            inventoryService.createInventory(request);

        assertEquals(productId, response.productId());
        assertEquals(storeId, response.storeId());
        assertEquals(100, response.quantity());
        assertEquals(20, response.reservedQuantity());
        assertEquals(80, response.availableQuantity());
        assertEquals(InventoryStatus.IN_STOCK, response.status());

        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void createInventoryThrowsWhenStoreDoesNotExist() {
        CreateInventoryRequest request = new CreateInventoryRequest(
            productId,
            storeId,
            100,
            0
        );

        when(storeRepository.existsById(storeId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.createInventory(request)
        );

        assertEquals(
            "Store not found with ID: " + storeId,
            exception.getMessage()
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void createInventoryThrowsWhenInventoryAlreadyExists() {
        CreateInventoryRequest request = new CreateInventoryRequest(
            productId,
            storeId,
            100,
            0
        );

        when(storeRepository.existsById(storeId)).thenReturn(true);
        when(inventoryRepository.existsByProductIdAndStoreId(
            productId,
            storeId
        )).thenReturn(true);

        assertThrows(
            DuplicateResourceException.class,
            () -> inventoryService.createInventory(request)
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void createInventoryThrowsWhenReservedQuantityExceedsQuantity() {
        CreateInventoryRequest request = new CreateInventoryRequest(
            productId,
            storeId,
            10,
            20
        );

        when(storeRepository.existsById(storeId)).thenReturn(true);
        when(inventoryRepository.existsByProductIdAndStoreId(
            productId,
            storeId
        )).thenReturn(false);

        InvalidInventoryOperationException exception = assertThrows(
            InvalidInventoryOperationException.class,
            () -> inventoryService.createInventory(request)
        );

        assertEquals(
            "Reserved quantity cannot exceed total quantity",
            exception.getMessage()
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void getInventoryByIdReturnsInventory() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));

        InventoryResponse response =
            inventoryService.getInventoryById(inventoryId);

        assertEquals(productId, response.productId());
        assertEquals(storeId, response.storeId());
        assertEquals(100, response.quantity());
        assertEquals(20, response.reservedQuantity());
        assertEquals(80, response.availableQuantity());
    }

    @Test
    void getInventoryByIdThrowsWhenInventoryDoesNotExist() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.getInventoryById(inventoryId)
        );

        assertEquals(
            "Inventory not found with ID: " + inventoryId,
            exception.getMessage()
        );
    }

    @Test
    void getInventoryByStoreReturnsInventoryList() {
        when(storeRepository.existsById(storeId)).thenReturn(true);
        when(inventoryRepository.findByStoreId(storeId))
            .thenReturn(List.of(inventory));

        List<InventoryResponse> responses =
            inventoryService.getInventoryByStore(storeId);

        assertEquals(1, responses.size());
        assertEquals(productId, responses.getFirst().productId());
        assertEquals(80, responses.getFirst().availableQuantity());
    }

    @Test
    void getInventoryByStoreThrowsWhenStoreDoesNotExist() {
        when(storeRepository.existsById(storeId)).thenReturn(false);

        assertThrows(
            ResourceNotFoundException.class,
            () -> inventoryService.getInventoryByStore(storeId)
        );

        verify(inventoryRepository, never()).findByStoreId(storeId);
    }

    @Test
    void getInventoryByProductReturnsInventoryList() {
        when(inventoryRepository.findByProductId(productId))
            .thenReturn(List.of(inventory));

        List<InventoryResponse> responses =
            inventoryService.getInventoryByProduct(productId);

        assertEquals(1, responses.size());
        assertEquals(storeId, responses.getFirst().storeId());
    }

    @Test
    void adjustInventoryIncreasesQuantity() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        InventoryResponse response = inventoryService.adjustInventory(
            inventoryId,
            new AdjustInventoryRequest(25)
        );

        assertEquals(125, response.quantity());
        assertEquals(20, response.reservedQuantity());
        assertEquals(105, response.availableQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void adjustInventoryThrowsWhenQuantityWouldBeNegative() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));

        InvalidInventoryOperationException exception = assertThrows(
            InvalidInventoryOperationException.class,
            () -> inventoryService.adjustInventory(
                inventoryId,
                new AdjustInventoryRequest(-101)
            )
        );

        assertEquals(
            "Inventory quantity cannot be reduced below zero",
            exception.getMessage()
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void adjustInventoryThrowsWhenQuantityWouldBeBelowReservedQuantity() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));

        InvalidInventoryOperationException exception = assertThrows(
            InvalidInventoryOperationException.class,
            () -> inventoryService.adjustInventory(
                inventoryId,
                new AdjustInventoryRequest(-90)
            )
        );

        assertEquals(
            "Inventory quantity cannot be less than reserved quantity",
            exception.getMessage()
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void reserveStockIncreasesReservedQuantity() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        InventoryResponse response = inventoryService.reserveStock(
            inventoryId,
            new StockQuantityRequest(10)
        );

        assertEquals(100, response.quantity());
        assertEquals(30, response.reservedQuantity());
        assertEquals(70, response.availableQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void reserveStockThrowsWhenNotEnoughInventoryIsAvailable() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));

        InvalidInventoryOperationException exception = assertThrows(
            InvalidInventoryOperationException.class,
            () -> inventoryService.reserveStock(
                inventoryId,
                new StockQuantityRequest(81)
            )
        );

        assertEquals(
            "Not enough available inventory to reserve 81 units",
            exception.getMessage()
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void releaseStockDecreasesReservedQuantity() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        InventoryResponse response = inventoryService.releaseStock(
            inventoryId,
            new StockQuantityRequest(5)
        );

        assertEquals(15, response.reservedQuantity());
        assertEquals(85, response.availableQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void releaseStockThrowsWhenQuantityExceedsReservedQuantity() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));

        InvalidInventoryOperationException exception = assertThrows(
            InvalidInventoryOperationException.class,
            () -> inventoryService.releaseStock(
                inventoryId,
                new StockQuantityRequest(21)
            )
        );

        assertEquals(
            "Cannot release more inventory than is currently reserved",
            exception.getMessage()
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void deleteInventoryChangesStatusToDiscontinued() {
        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        inventoryService.deleteInventory(inventoryId);

        assertEquals(
            InventoryStatus.DISCONTINUED,
            inventory.getStatus()
        );

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void discontinuedInventoryCannotBeAdjusted() {
        inventory.setStatus(InventoryStatus.DISCONTINUED);

        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));

        InvalidInventoryOperationException exception = assertThrows(
            InvalidInventoryOperationException.class,
            () -> inventoryService.adjustInventory(
                inventoryId,
                new AdjustInventoryRequest(10)
            )
        );

        assertEquals(
            "Cannot modify discontinued inventory",
            exception.getMessage()
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void discontinuedInventoryCannotBeReserved() {
        inventory.setStatus(InventoryStatus.DISCONTINUED);

        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));

        assertThrows(
            InvalidInventoryOperationException.class,
            () -> inventoryService.reserveStock(
                inventoryId,
                new StockQuantityRequest(5)
            )
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void discontinuedInventoryCannotBeReleased() {
        inventory.setStatus(InventoryStatus.DISCONTINUED);

        when(inventoryRepository.findById(inventoryId))
            .thenReturn(Optional.of(inventory));

        assertThrows(
            InvalidInventoryOperationException.class,
            () -> inventoryService.releaseStock(
                inventoryId,
                new StockQuantityRequest(5)
            )
        );

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }
}


