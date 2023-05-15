package com.logistics.supply.service;

import com.logistics.supply.dto.StoreDTO;
import com.logistics.supply.exception.NotFoundException;
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

      Optional<Store> result = storeRepository.findByDepartment(department);
      return result.orElseThrow(() -> new NotFoundException("Store for department %s not found".formatted(department.getName())));
  }

  public Store save(StoreDTO store) {
      Store s = new Store(store.getName(), store.getDepartment());
      return storeRepository.save(s);
  }
}
