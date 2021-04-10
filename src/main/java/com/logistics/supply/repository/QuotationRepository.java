package com.logistics.supply.repository;

import com.logistics.supply.model.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Integer> {


  List<Quotation> findBySupplierId(int supplierId);


  Quotation findByRequestDocumentId(int requestDocumentId);
}
