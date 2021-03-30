package com.logistics.supply.repository;

import com.logistics.supply.model.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Integer> {
}
