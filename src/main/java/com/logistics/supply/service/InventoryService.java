package com.logistics.supply.service;

import com.logistics.supply.model.Inventory;
import com.logistics.supply.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InventoryService {

  @Autowired InventoryRepository inventoryRepository;

  public Inventory add(Inventory inventory) {
    try {
      return inventoryRepository.save(inventory);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public Optional<Inventory> getById(Integer inventoryId) {
    try {
      return inventoryRepository.findById(inventoryId);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public void deleteById(int inventoryId) {
    try {
      inventoryRepository.deleteById(inventoryId);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  public List<Inventory> getAll() {
    List<Inventory> inventories = new ArrayList<>();
    try {
      List<Inventory> list = inventoryRepository.findAll();
      list.forEach(inventories::add);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return inventories;
  }
}
