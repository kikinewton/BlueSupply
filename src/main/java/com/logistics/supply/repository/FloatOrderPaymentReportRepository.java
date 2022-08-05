package com.logistics.supply.repository;

import com.logistics.supply.model.FloatOrderPaymentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface FloatOrderPaymentReportRepository extends ReadOnlyRepository<FloatOrderPaymentReport, Long>{
    Page<FloatOrderPaymentReport> findByFundsAllocatedDateBetween(
      Date fundsAllocatedDateStart, Date fundsAllocatedDateEnd, Pageable pageable);

  Page<FloatOrderPaymentReport> findByRequesterStaffIdEqualsIgnoreCase(
      String requesterStaffId, Pageable pageable);


  @Query("select f.id, f.floatOrderRef, f.requestedAmount, f.floatType, f.paidAmount, f.requestedDate, f.requesterStaffId, f.department, f.endorsementDate, f.endorsedBy, f.approvalDate, f.approvedBy, f.fundsAllocatedDate, f.accountOfficer, f.retirementDate, f.retired from FloatOrderPaymentReport f where f.fundsAllocatedDate between ?1 and ?2")
  List<Object[]> getByFundsAllocatedDateBetween(
      Date fundsAllocatedDateStart, Date fundsAllocatedDateEnd);
}
