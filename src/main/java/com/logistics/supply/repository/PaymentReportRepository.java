package com.logistics.supply.repository;

import com.logistics.supply.model.PaymentReport;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentReportRepository extends ReadOnlyRepository<PaymentReport, Long> {

}
