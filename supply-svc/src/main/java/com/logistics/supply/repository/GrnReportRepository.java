package com.logistics.supply.repository;

import com.logistics.supply.model.GrnReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface GrnReportRepository
    extends ReadOnlyRepository<GrnReport, Long>, JpaSpecificationExecutor<GrnReport> {

  @Query(
      value = "select * from grn_report g where g.date_received between :startDate and :endDate",
      countQuery =
          "select count(g.id) from grn_report g where g.date_received between :startDate and :endDate",
      nativeQuery = true)
  Page<GrnReport> findByDateReceivedBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

  @Query(
      value =
          "select * from grn_report g where g.date_received between :startDate and :endDate and upper(g.supplier) LIKE CONCAT('%',upper(:supplier),'%')",
      countQuery =
          "select count(*) from grn_report g where g.date_received between :startDate and :endDate and upper(g.supplier) LIKE CONCAT('%',upper(:supplier),'%')",
      nativeQuery = true)
  Page<GrnReport> findByDateReceivedBetweenAndSupplierIgnoreCase(
          @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("supplier") String supplier, Pageable pageable);
}
