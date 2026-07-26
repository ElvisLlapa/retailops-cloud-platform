package com.elvisllapa.retailops.inventory.repository;

import com.elvisllapa.retailops.inventory.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByStoreNumber(String storeNumber);

    boolean existsByStoreNumber(String storeNumber);
}