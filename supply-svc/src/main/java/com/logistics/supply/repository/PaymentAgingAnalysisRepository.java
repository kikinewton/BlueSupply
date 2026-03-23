package com.logistics.supply.repository;

import com.logistics.supply.model.PaymentAgingAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAgingAnalysisRepository extends JpaRepository<PaymentAgingAnalysis, Long> {
}
