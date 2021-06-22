package com.logistics.supply.service;

import com.logistics.supply.dto.*;
import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.repository.RequestPerMonthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService extends AbstractDataService {

  @Autowired public RequestPerMonthRepository requestPerMonthRepository;

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
      e.printStackTrace();
    }
    return spendAnalysis;
  }

  public int countOfPaymentDueWithinOneWeek() {
    int count = paymentRepository.findCountOfPaymentsDueWithinOneWeek();
    if (Objects.isNull(count)) return 0;
    return count;
  }

  public int countOfGRNForToday() {
    int count = goodsReceivedNoteRepository.findCountOfGRNForToday();
    if (Objects.isNull(count)) return 0;
    return count;
  }

  public int countPaymentsMadeToday() {
    int count = paymentRepository.findCountOfPaymentMadeToday();
    if (Objects.isNull(count)) return 0;
    return count;
  }

  public int countofRequestPerCurrentMonth() {
    int count = requestItemRepository.totalRequestPerCurrentMonth();
    if (Objects.isNull(count)) return 0;
    return count;
  }

  public List<RequestPerUserDepartment> findApprovedNumberRequestItemsAndUserDepartmentToday() {
    List<RequestPerUserDepartment> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.findApprovedRequestPerUserDepartmentToday());
      return items;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return items;
  }

  public List<RequestPerCategory> findRequestPerCategoryForToday() {
    List<RequestPerCategory> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.findApprovedRequestPerCategory());
      return items;
    } catch (Exception e) {
      e.printStackTrace();
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
      e.printStackTrace();
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
      e.printStackTrace();
    }
    return null;
  }
}
