package com.logistics.supply.repository;

import com.logistics.supply.model.PaymentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PaymentReportRepository extends ReadOnlyRepository<PaymentReport, Long> {

  @Query(
      value =
          "select * from payment_report p where p.payment_date between cast(:startDate as DATE) and cast(:endDate as DATE)",
      countQuery =
          "select count(*) from payment_report p where p.payment_date between cast(:startDate as DATE) and cast(:endDate as DATE)",
      nativeQuery = true)
  Page<PaymentReport> findByDateReceivedBetween(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

  Page<PaymentReport> findByPaymentDateBetween(Date startDate,Date endDate, Pageable pageable);

  @Query(
      value =
          "select * from payment_report p where p.payment_date between cast(:startDate as DATE) and cast(:endDate as DATE)",
      nativeQuery = true)
  List<Object[]> findAllByPaymentDateBetween(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query(
      value =
          "select * from payment_report p where p.payment_date between :startDate and :endDate and upper(p.supplier) LIKE CONCAT('%',upper(:supplier),'%')",
      countQuery =
          "select count(*) from payment_report p where p.payment_date between :startDate and :endDate and upper(p.supplier) LIKE CONCAT('%',upper(:supplier),'%')",
      nativeQuery = true)
  Page<PaymentReport> findByDateReceivedBetweenAndSupplierIgnoreCase(
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate,
      @Param("supplier") String supplier,
      Pageable pageable);
}
