package com.logistics.supply.repository;

import com.logistics.supply.dto.FloatSpendByDepartment;
import com.logistics.supply.dto.FloatSpendByType;
import com.logistics.supply.model.FloatOrderPaymentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public interface FloatOrderPaymentReportRepository extends ReadOnlyRepository<FloatOrderPaymentReport, Long> {
    Page<FloatOrderPaymentReport> findByFundsAllocatedDateBetween(
      Date fundsAllocatedDateStart, Date fundsAllocatedDateEnd, Pageable pageable);

  Page<FloatOrderPaymentReport> findByRequesterStaffIdEqualsIgnoreCase(
      String requesterStaffId, Pageable pageable);


  @Query("select f.id, f.floatOrderRef, f.requestedAmount, f.floatType, f.paidAmount, f.requestedDate, f.requesterStaffId, f.requestedBy, f.createdBy,f.department, f.endorsementDate, f.endorsedBy, f.approvalDate, f.approvedBy, f.fundsAllocatedDate, f.accountOfficer, f.retired, f.retirementDate from FloatOrderPaymentReport f where f.fundsAllocatedDate between ?1 and ?2")
  List<Object[]> getByFundsAllocatedDateBetween(
      Date fundsAllocatedDateStart, Date fundsAllocatedDateEnd);

  @Query(value = "SELECT COALESCE(SUM(paid_amount), 0) FROM float_payment_report WHERE funds_allocated_date >= date_trunc('month', current_date)", nativeQuery = true)
  BigDecimal totalSpendThisMonth();

  @Query(value = "SELECT department, SUM(paid_amount) AS total_spend FROM float_payment_report WHERE funds_allocated_date >= date_trunc('month', current_date) GROUP BY department ORDER BY total_spend DESC", nativeQuery = true)
  List<FloatSpendByDepartment> spendByDepartmentThisMonth();

  @Query(value = "SELECT float_type, SUM(paid_amount) AS total_spend FROM float_payment_report WHERE funds_allocated_date >= date_trunc('month', current_date) GROUP BY float_type ORDER BY total_spend DESC", nativeQuery = true)
  List<FloatSpendByType> spendByTypeThisMonth();
}
