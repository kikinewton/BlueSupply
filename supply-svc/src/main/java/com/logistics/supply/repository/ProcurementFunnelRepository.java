package com.logistics.supply.repository;

import com.logistics.supply.model.ProcurementFunnel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcurementFunnelRepository extends JpaRepository<ProcurementFunnel, Integer> {
}
