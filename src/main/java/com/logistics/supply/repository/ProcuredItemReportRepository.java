package com.logistics.supply.repository;

import com.logistics.supply.model.ProcuredItemReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ProcuredItemReportRepository extends ReadOnlyRepository<ProcuredItemReport, Long> {

  @Query(
      value =
          "select * from procured_item_report p where p.grn_issued_date between :startDate and :endDate and upper(p.supplied_by) LIKE CONCAT('%',upper(:suppliedBy),'%')",
      countQuery =
          "select count(*) from procured_item_report p where p.grn_issued_date between :startDate and :endDate and upper(p.supplied_by) LIKE CONCAT('%',upper(:suppliedBy),'%')",
      nativeQuery = true)
  Page<ProcuredItemReport> findBySupplier(
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("suppliedBy") String suppliedBy,
      Pageable pageable);

  @Query(
      value =
          "select * from procured_item_report p where p.grn_issued_date between :startDate and :endDate",
      countQuery =
          "select count(*) from procured_item_report p where p.grn_issued_date between :startDate and :endDate",
      nativeQuery = true)
  Page<ProcuredItemReport> findBetweenDate(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);
}
