package com.logistics.supply.repository;

import com.logistics.supply.model.RequestForQuotation;

import com.logistics.supply.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestForQuotationRepository extends JpaRepository<RequestForQuotation, Integer> {
    Optional<RequestForQuotation> findByQuotationReceivedFalseAndSupplier(Supplier supplier);
}
