package com.logistics.supply.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistics.supply.model.RequestCategory;

public interface RequestCategoryRepository extends JpaRepository<RequestCategory, Integer> {
}
