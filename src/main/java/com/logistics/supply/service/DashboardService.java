package com.logistics.supply.service;

import com.logistics.supply.dto.*;
import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.repository.PaymentRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.repository.RequestPerMonthRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


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
    int count = goodsReceivedNoteRepository.findNumberOfPaymentDueInOneWeek().size();
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
      return data;

    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }
}
