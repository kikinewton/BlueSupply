package com.logistics.supply.repository;

import com.logistics.supply.model.RequestCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestCategoryRepository extends JpaRepository<RequestCategory, Integer> {
}
