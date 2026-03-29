package com.logistics.supply.service;

import com.logistics.supply.dto.*;
import com.logistics.supply.interfaces.projections.CancellationRateProjection;
import com.logistics.supply.interfaces.projections.CycleTimeProjection;
import com.logistics.supply.interfaces.projections.MonthlyTrendProjection;
import com.logistics.supply.interfaces.projections.PaymentMade;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.CancelledRequestItemRepository;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentRepository;
import com.logistics.supply.repository.FloatAgingAnalysisRepository;
import com.logistics.supply.repository.FloatOrderPaymentReportRepository;
import com.logistics.supply.repository.FloatOrderRepository;
import com.logistics.supply.repository.PettyCashPaymentReportRepository;
import com.logistics.supply.repository.PettyCashRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.repository.RequestPerMonthRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService {
  private final RequestItemRepository requestItemRepository;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  private final PaymentRepository paymentRepository;
  private final RequestPerMonthRepository requestPerMonthRepository;
  private final CancelledRequestItemRepository cancelledRequestItemRepository;
  private final PettyCashRepository pettyCashRepository;
  private final PettyCashPaymentReportRepository pettyCashPaymentReportRepository;
  private final FloatOrderRepository floatOrderRepository;
  private final FloatAgingAnalysisRepository floatAgingAnalysisRepository;
  private final FloatOrderPaymentReportRepository floatOrderPaymentReportRepository;

  public List<RequestPerCurrentMonthPerDepartment> getAllRequestPerDepartmentForMonth() {
    return requestPerMonthRepository.findAll();
  }

  public List<SpendAnalysisDTO> getSupplierSpendAnalysis() {
    return requestItemRepository.supplierSpendAnalysis();
  }

  public int countOfPaymentDueWithinOneWeek() {
    return goodsReceivedNoteRepository.findNumberOfPaymentDueInOneWeek();
  }

  public int countOfGRNForToday() {
    return goodsReceivedNoteRepository.findCountOfGRNForToday();
  }

  public int countPaymentsMadeToday() {
    return paymentRepository.findCountOfPaymentMadeToday();
  }

  public int countofRequestPerCurrentMonth() {
    return requestItemRepository.totalRequestPerCurrentMonth();
  }

  public List<RequestPerUserDepartment>
      findApprovedNumberRequestItemsAndUserDepartmentPastSevenDays() {
    return requestItemRepository.findApprovedRequestPerUserDepartmentToday();
  }

  public List<RequestPerCategory> findRequestPerCategoryForPastSevenDays() {
    return requestItemRepository.findApprovedRequestPerCategory();
  }

  public List<CostOfGoodsPerDepartmentPerMonth> findCostPerDepartmentForCurrentMonth() {
    List<CostOfGoodsPerDepartmentPerMonth> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.findCostOfGoodsPaidPerDepartmentPerMonth());
      return items;
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return items;
  }

  public DashboardData getDashboardData() {
    DashboardData data = new DashboardData();
    try { data.setCountOfGRNForToday(countOfGRNForToday()); }
    catch (Exception e) { log.warn("countOfGRNForToday failed: {}", e.getMessage()); }
    try { data.setApprovedNumberRequestItemsAndUserDepartmentToday(
        findApprovedNumberRequestItemsAndUserDepartmentPastSevenDays()); }
    catch (Exception e) { log.warn("approvedRequestPerUserDepartment failed: {}", e.getMessage()); }
    try { data.setCostPerDepartmentForCurrentMonth(findCostPerDepartmentForCurrentMonth()); }
    catch (Exception e) { log.warn("costPerDepartmentForCurrentMonth failed: {}", e.getMessage()); }
    try { data.setCountOfPaymentDueWithinOneWeek(countOfPaymentDueWithinOneWeek()); }
    catch (Exception e) { log.warn("countOfPaymentDueWithinOneWeek failed: {}", e.getMessage()); }
    try { data.setCountOfRequestPerCurrentMonth(countofRequestPerCurrentMonth()); }
    catch (Exception e) { log.warn("countofRequestPerCurrentMonth failed: {}", e.getMessage()); }
    try { data.setCountPaymentsMadeToday(countPaymentsMadeToday()); }
    catch (Exception e) { log.warn("countPaymentsMadeToday failed: {}", e.getMessage()); }
    try { data.setRequestPerCategoryForToday(findRequestPerCategoryForPastSevenDays()); }
    catch (Exception e) { log.warn("requestPerCategoryForToday failed: {}", e.getMessage()); }
    try { data.setSupplierSpendAnalysis(getSupplierSpendAnalysis()); }
    catch (Exception e) { log.warn("supplierSpendAnalysis failed: {}", e.getMessage()); }
    try { data.setRequestsPerCurrentMonthPerDepartment(getAllRequestPerDepartmentForMonth()); }
    catch (Exception e) { log.warn("requestsPerCurrentMonthPerDepartment failed: {}", e.getMessage()); }
    try { data.setRequestForThisMonth(requestForThisMonth()); }
    catch (Exception e) { log.warn("requestForThisMonth failed: {}", e.getMessage()); }
    try { data.setPaymentsMadeToday(paymentsMadeToday()); }
    catch (Exception e) { log.warn("paymentsMadeToday failed: {}", e.getMessage()); }
    try { data.setPaymentDueInAWeek(paymentDueInAWeek()); }
    catch (Exception e) { log.warn("paymentDueInAWeek failed: {}", e.getMessage()); }
    try { data.setGrnIssuedToday(grnIssuedToday()); }
    catch (Exception e) { log.warn("grnIssuedToday failed: {}", e.getMessage()); }
    data.setCountPettyCashPendingEndorsement(countPettyCashPendingEndorsement());
    data.setCountPettyCashAwaitingApproval(countPettyCashAwaitingApproval());
    data.setCountPettyCashPendingPayment(countPettyCashPendingPayment());
    data.setPettyCashSpendThisMonth(pettyCashSpendThisMonth());
    data.setPettyCashSpendByDepartment(pettyCashSpendByDepartment());
    data.setPettyCashTopPurposes(pettyCashTopPurposes());
    data.setCountFloatPendingEndorsement(countFloatPendingEndorsement());
    data.setCountFloatAwaitingApproval(countFloatAwaitingApproval());
    data.setCountFloatFundsAllocatedNotRetired(countFloatFundsAllocatedNotRetired());
    data.setCountFloatFlagged(countFloatFlagged());
    data.setCountFloatAwaitingAuditorRetirement(countFloatAwaitingAuditorRetirement());
    data.setCountFloatAwaitingGmRetirement(countFloatAwaitingGmRetirement());
    data.setFloatSpendThisMonth(floatSpendThisMonth());
    data.setFloatSpendByDepartment(floatSpendByDepartment());
    data.setFloatSpendByType(floatSpendByType());
    data.setFloatAgingDistribution(floatAgingDistribution());
    return data;
  }

  // request this month
  public List<ItemRequest> requestForThisMonth() {
    try {
      return requestItemRepository.requestForCurrentMonth().stream()
          .map(
              r -> {
                ItemRequest request = new ItemRequest();
                BeanUtils.copyProperties(r, request);
                return request;
              })
          .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return new ArrayList<>();
  }

  // payments due in one week

  public List<GRN> paymentDueInAWeek() {
    List<GoodsReceivedNote> grn = new ArrayList<>();
    try {
      grn.addAll(goodsReceivedNoteRepository.findPaymentDueInOneWeek());
      List<GRN> grnList =
          grn.stream()
              .map(
                  g -> {
                    GRN grn1 = new GRN();
                    BeanUtils.copyProperties(g, grn1);
                    return grn1;
                  })
              .collect(Collectors.toList());
      return grnList;
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return new ArrayList<>();
  }

  // payments made today

  public List<PaymentMade> paymentsMadeToday() {
    return paymentRepository.findAllPaymentMadeToday();
  }

  // grn for today

  public List<GRN> grnIssuedToday() {
    List<GoodsReceivedNote> grn = new ArrayList<>();
    try {
      grn.addAll(goodsReceivedNoteRepository.findGRNIssuedToday());
      List<GRN> grnList =
          grn.stream()
              .map(
                  g -> {
                    GRN grn1 = new GRN();
                    BeanUtils.copyProperties(g, grn1);
                    return grn1;
                  })
              .collect(Collectors.toList());
      return grnList;
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return new ArrayList<>();
  }


  public List<CycleTimeProjection> getProcurementCycleTime() {
    try {
      return requestItemRepository.findCycleTimeByDepartment();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return new ArrayList<>();
  }

  public List<CancellationRateProjection> getCancellationRate() {
    try {
      return cancelledRequestItemRepository.findCancellationRateByDepartment();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return new ArrayList<>();
  }

  public List<MonthlyTrendProjection> getMonthlyTrends(int months) {
    try {
      return requestItemRepository.findMonthlyTrends(months);
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return new ArrayList<>();
  }

  public long countFloatPendingEndorsement() {
    try {
      return floatOrderRepository.countByEndorsement(com.logistics.supply.enums.EndorsementStatus.PENDING);
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return 0;
  }

  public long countFloatAwaitingApproval() {
    try {
      return floatOrderRepository.countByEndorsementAndApproval(
          com.logistics.supply.enums.EndorsementStatus.ENDORSED,
          com.logistics.supply.enums.RequestApproval.PENDING);
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return 0;
  }

  public long countFloatFundsAllocatedNotRetired() {
    try {
      return floatOrderRepository.countByFundsReceivedTrueAndRetiredFalse();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return 0;
  }

  public long countFloatFlagged() {
    try {
      return floatOrderRepository.countByFlaggedTrue();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return 0;
  }

  public long countFloatAwaitingAuditorRetirement() {
    try {
      return floatOrderRepository.countByAuditorRetirementApproval(false);
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return 0;
  }

  public long countFloatAwaitingGmRetirement() {
    try {
      return floatOrderRepository.countByGmRetirementApproval(false);
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return 0;
  }

  public BigDecimal floatSpendThisMonth() {
    try {
      BigDecimal result = floatOrderPaymentReportRepository.totalSpendThisMonth();
      return result != null ? result : BigDecimal.ZERO;
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return BigDecimal.ZERO;
  }

  public List<FloatSpendByDepartment> floatSpendByDepartment() {
    try {
      return floatOrderPaymentReportRepository.spendByDepartmentThisMonth();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return Collections.emptyList();
  }

  public List<FloatSpendByType> floatSpendByType() {
    try {
      return floatOrderPaymentReportRepository.spendByTypeThisMonth();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return Collections.emptyList();
  }

  public List<FloatAgingBucket> floatAgingDistribution() {
    try {
      return floatAgingAnalysisRepository.getAgingDistribution();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return Collections.emptyList();
  }

  public int countPettyCashPendingEndorsement() {
    try {
      return pettyCashRepository.countPendingEndorsement();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return 0;
  }

  public int countPettyCashAwaitingApproval() {
    try {
      return pettyCashRepository.countAwaitingApproval();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return 0;
  }

  public int countPettyCashPendingPayment() {
    try {
      return pettyCashRepository.countPendingPayment();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return 0;
  }

  public BigDecimal pettyCashSpendThisMonth() {
    try {
      BigDecimal result = pettyCashPaymentReportRepository.totalSpendThisMonth();
      return result != null ? result : BigDecimal.ZERO;
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return BigDecimal.ZERO;
  }

  public List<PettyCashSpendByDepartment> pettyCashSpendByDepartment() {
    try {
      return pettyCashPaymentReportRepository.spendByDepartmentThisMonth();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return Collections.emptyList();
  }

  public List<PettyCashTopPurpose> pettyCashTopPurposes() {
    try {
      return pettyCashPaymentReportRepository.topPurposesThisMonth();
    } catch (Exception e) {
      log.error("Dashboard data query failed", e);
    }
    return Collections.emptyList();
  }

  @Data
  @NoArgsConstructor
  public static class GRN {
    //    Employee createdBy;
    private Supplier finalSupplier;
    private LocalPurchaseOrder localPurchaseOrder;
    private List<RequestItem> receivedItems;
    private Date paymentDate;
    private BigDecimal invoiceAmountPayable;
  }
}
