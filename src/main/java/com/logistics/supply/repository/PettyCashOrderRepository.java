package com.logistics.supply.repository;

import com.logistics.supply.model.PettyCashOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PettyCashOrderRepository extends JpaRepository<PettyCashOrder, Integer>, JpaSpecificationExecutor<PettyCashOrder> {
    @Query(value = "Select count(id) from petty_cash_order", nativeQuery = true)
    long countAll();
}
