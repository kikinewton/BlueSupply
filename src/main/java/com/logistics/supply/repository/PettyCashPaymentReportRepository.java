package com.logistics.supply.repository;

import com.logistics.supply.model.PettyCashPaymentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PettyCashPaymentReportRepository
    extends ReadOnlyRepository<PettyCashPaymentReport, String> {

  @Query(
      value =
          "select * from petty_cash_payment_report p where p.payment_date between cast(:startDate as date) and cast(:endDate as date)",
      countQuery =
          "select count(*) from petty_cash_payment_report p where p.payment_date between cast(:startDate as date) and cast(:endDate as date)",
      nativeQuery = true)
  Page<PettyCashPaymentReport> findByPaymentDateBetween(
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      Pageable pageable);

  @Query(
      value =
          "select * from petty_cash_payment_report p where p.payment_date between cast(:startDate as date) and cast(:endDate as date) and upper(p.requested_by_email) LIKE CONCAT('%',upper(:requestedByEmail),'%')",
      countQuery =
          "select count(*) from petty_cash_payment_report p where p.payment_date between cast(:startDate as date) and cast(:endDate as date) and upper(p.requested_by_email) LIKE CONCAT('%',upper(:requestedByEmail),'%')",
      nativeQuery = true)
  Page<PettyCashPaymentReport> findByPaymentDateBetweenAndRequestedByEmail(
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("requestedByEmail") String requestedByEmail,
      Pageable pageable);

  @Query(
      value =
          "select * from petty_cash_payment_report p where p.payment_date between cast(:startDate as date) and cast(:endDate as date)",
      nativeQuery = true)
  List<Object[]> getPaymentReport(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
