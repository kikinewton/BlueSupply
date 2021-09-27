package com.logistics.supply.repository;

import com.logistics.supply.model.ProcuredItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcuredItemRepository extends JpaRepository<ProcuredItem, Integer> {}
