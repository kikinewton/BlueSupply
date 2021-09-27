package com.logistics.supply.repository;

import com.logistics.supply.model.CancelledRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelledRequestItemRepository extends JpaRepository<CancelledRequestItem, Integer> {

}
