package com.logistics.supply.repository;


import com.logistics.supply.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findById(Integer inventoryId);
}
