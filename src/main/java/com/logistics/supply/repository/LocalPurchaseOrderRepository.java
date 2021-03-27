package com.logistics.supply.repository;

import com.logistics.supply.model.LocalPurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalPurchaseOrderRepository extends JpaRepository<LocalPurchaseOrder, Long> {
}
