package com.logistics.supply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.CancelledRequestItem;

@Repository
public interface CancelledRequestItemRepository extends JpaRepository<CancelledRequestItem, Integer> {

}
