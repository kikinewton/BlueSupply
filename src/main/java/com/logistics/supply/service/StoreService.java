package com.logistics.supply.service;

import com.logistics.supply.dto.StoreDTO;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Store;
import com.logistics.supply.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreService {
  private final StoreRepository storeRepository;

  public List<Store> findAll() {
    return storeRepository.findAll();
  }

  public Store findByDepartment(Department department) {
    try {
      Optional<Store> result = storeRepository.findByDepartment(department);
      if (result.isPresent()) return result.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Store save(StoreDTO store) {
    try {
      Store s = new Store(store.getName(), store.getDepartment());
      return storeRepository.save(s);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }
}
