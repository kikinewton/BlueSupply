package com.logistics.supply.service;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.repository.RequestPerMonthRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardService {

  @Autowired RequestItemRepository requestItemRepository;
  @Autowired GoodsReceivedNoteRepository goodsReceivedNoteRepository;
  @Autowired PaymentRepository paymentRepository;
  @Autowired RequestPerMonthRepository requestPerMonthRepository;

  public List<RequestPerCurrentMonthPerDepartment> getAllRequestPerDepartmentForMonth() {
    List<RequestPerCurrentMonthPerDepartment> requests = requestPerMonthRepository.findAll();
    return requests;
  }

  public List<SpendAnalysisDTO> getSupplierSpendAnalysis() {
    List<SpendAnalysisDTO> spendAnalysis = new ArrayList<>();
    try {
      spendAnalysis.addAll(requestItemRepository.supplierSpendAnalysis());
      return spendAnalysis;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return spendAnalysis;
  }

  public int countOfPaymentDueWithinOneWeek() {
    int count = goodsReceivedNoteRepository.findPaymentDueInOneWeek().size();
    return count;
  }

  public int countOfGRNForToday() {
    int count = goodsReceivedNoteRepository.findCountOfGRNForToday();
    return count;
  }

  public int countPaymentsMadeToday() {
    int count = paymentRepository.findCountOfPaymentMadeToday();
    return count;
  }

  public int countofRequestPerCurrentMonth() {
    int count = requestItemRepository.totalRequestPerCurrentMonth();
    return count;
  }

  public List<RequestPerUserDepartment> findApprovedNumberRequestItemsAndUserDepartmentToday() {
    List<RequestPerUserDepartment> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.findApprovedRequestPerUserDepartmentToday());
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

  public List<RequestPerCategory> findRequestPerCategoryForToday() {
    List<RequestPerCategory> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.findApprovedRequestPerCategory());
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

  public List<CostOfGoodsPerDepartmentPerMonth> findCostPerDepartmentForCurrentMonth() {
    List<CostOfGoodsPerDepartmentPerMonth> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.findCostOfGoodsPaidPerDepartmentPerMonth());
      items.forEach(System.out::println);
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
          findApprovedNumberRequestItemsAndUserDepartmentToday());
      data.setCostPerDepartmentForCurrentMonth(findCostPerDepartmentForCurrentMonth());
      data.setCountOfPaymentDueWithinOneWeek(countOfPaymentDueWithinOneWeek());
      data.setCountOfRequestPerCurrentMonth(countofRequestPerCurrentMonth());
      data.setCountPaymentsMadeToday(countPaymentsMadeToday());
      data.setRequestPerCategoryForToday(findRequestPerCategoryForToday());
      data.setSupplierSpendAnalysis(getSupplierSpendAnalysis());
      data.setRequestsPerCurrentMonthPerDepartment(getAllRequestPerDepartmentForMonth());
      data.setRequestForThisMonth(requestForThisMonth());
      data.setPaymentsMadeToday(paymentsMadeToday());
      data.setPaymentDueInAWeek(paymentDueInAWeek());
      data.setGrnIssuedToday(grnIssuedToday());
      return data;

    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  // request this month
  public List<ItemRequest> requestForThisMonth() {
    List<RequestItem> requestItems = new ArrayList<>();
    try {
      requestItems.addAll(requestItemRepository.requestForCurrentMonth());
      List<ItemRequest> requestList =
          requestItems.stream()
              .map(
                  r -> {
                    ItemRequest request = new ItemRequest();
                    BeanUtils.copyProperties(r, request);
                    return request;
                  })
              .collect(Collectors.toList());
      return requestList;
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
    List<Payment> payments = new ArrayList<>();
    try {
      payments.addAll(paymentRepository.findAllPaymentMadeToday());
      List<PaymentMade> pay =
          payments.stream()
              .map(
                  p -> {
                    PaymentMade paymentMade = new PaymentMade();
                    BeanUtils.copyProperties(p, paymentMade);
                    return paymentMade;
                  })
              .collect(Collectors.toList());
      return pay;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
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
  public static class ItemRequest {
    private String requestItemRef;
    private RequestReason reason;
    private Date createdDate;
    private String purpose;
    private int quantity;
    private String name;
    private Department userDepartment;
    private RequestCategory requestCategory;
  }

  @Data
  @NoArgsConstructor
  public static class GRN {
    Employee createdBy;
    private Supplier finalSupplier;
    private LocalPurchaseOrder localPurchaseOrder;
    private List<RequestItem> receivedItems;
    private Date paymentDate;
    private Invoice invoice;
    private String grnRef;
    private BigDecimal invoiceAmountPayable;
    private List<Payment> paymentHistory;
  }

  @Data
  @NoArgsConstructor
  public static class PaymentMade {
    private String purchaseNumber;
    private BigDecimal paymentAmount;
    private PaymentStatus paymentStatus;
    private String chequeNumber;
    private String bank;
  }
}
