package com.logistics.supply.repository;

import com.logistics.supply.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.Store;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {
  Optional<Store> findByDepartment(Department department);
}
