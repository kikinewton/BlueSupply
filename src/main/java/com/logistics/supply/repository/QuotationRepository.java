package com.logistics.supply.repository;

import com.logistics.supply.model.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Integer> {

  @Query(
      value = "Select * from quotation where supplier_id =:supplierId order by created_date desc",
      nativeQuery = true)
  List<Quotation> findBySupplierOrderByCreatedDateDesc(@Param("supplierId") int supplierId);
}
