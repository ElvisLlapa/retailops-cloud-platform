package com.elvisllapa.retailops.inventory.repository;

import com.elvisllapa.retailops.inventory.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    List<Inventory> findByStoreId(UUID storeId);

    List<Inventory> findByProductId(UUID productId);

    Optional<Inventory> findByProductIdAndStoreId(
        UUID productId,
        UUID storeId
    );

    boolean existsByProductIdAndStoreId(
        UUID productId,
        UUID storeId
    );
}