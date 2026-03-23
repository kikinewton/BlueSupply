package com.logistics.supply.repository;

import com.logistics.supply.model.SpendByCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpendByCategoryRepository extends JpaRepository<SpendByCategory, Integer> {
}
