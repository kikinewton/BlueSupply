package com.logistics.supply.service;

import com.logistics.supply.dto.*;
import com.logistics.supply.exception.DashboardException;
import com.logistics.supply.interfaces.projections.PaymentMade;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentRepository;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService {
  private final RequestItemRepository requestItemRepository;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  private final PaymentRepository paymentRepository;
  private final RequestPerMonthRepository requestPerMonthRepository;

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
      log.error(e.toString());
    }
    return items;
  }

  public DashboardData getDashboardData() {
    DashboardData data = new DashboardData();
    try {
      data.setCountOfGRNForToday(countOfGRNForToday());
      data.setApprovedNumberRequestItemsAndUserDepartmentToday(
          findApprovedNumberRequestItemsAndUserDepartmentPastSevenDays());
      data.setCostPerDepartmentForCurrentMonth(findCostPerDepartmentForCurrentMonth());
      data.setCountOfPaymentDueWithinOneWeek(countOfPaymentDueWithinOneWeek());
      data.setCountOfRequestPerCurrentMonth(countofRequestPerCurrentMonth());
      data.setCountPaymentsMadeToday(countPaymentsMadeToday());
      data.setRequestPerCategoryForToday(findRequestPerCategoryForPastSevenDays());
      data.setSupplierSpendAnalysis(getSupplierSpendAnalysis());
      data.setRequestsPerCurrentMonthPerDepartment(getAllRequestPerDepartmentForMonth());
      data.setRequestForThisMonth(requestForThisMonth());
      data.setPaymentsMadeToday(paymentsMadeToday());
      data.setPaymentDueInAWeek(paymentDueInAWeek());
      data.setGrnIssuedToday(grnIssuedToday());
      return data;

    } catch (Exception e) {
      log.error(e.toString());
      throw new DashboardException("Error in extracting dashboard data");
    }
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
      log.error(e.toString());
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
      log.error(e.toString());
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
      log.error(e.toString());
    }
    return new ArrayList<>();
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
