package com.logistics.supply.repository;

import com.logistics.supply.model.Department;
import com.logistics.supply.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {
  Optional<Store> findByDepartment(Department department);
}
