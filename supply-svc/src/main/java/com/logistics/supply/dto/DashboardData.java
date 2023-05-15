package com.logistics.supply.dto;

import com.logistics.supply.interfaces.projections.PaymentMade;
import com.logistics.supply.service.DashboardService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
public class DashboardData {

  private List<SpendAnalysisDTO> supplierSpendAnalysis;
  private List<RequestPerCurrentMonthPerDepartment> requestsPerCurrentMonthPerDepartment;
  private List<DashboardService.GRN> paymentDueInAWeek;
  private List<DashboardService.GRN> grnIssuedToday;
  private int countOfPaymentDueWithinOneWeek;
  private int countOfGRNForToday;
  private int countPaymentsMadeToday;
  private int countOfRequestPerCurrentMonth;
  private List<RequestPerUserDepartment> approvedNumberRequestItemsAndUserDepartmentToday;
  private List<RequestPerCategory> requestPerCategoryForToday;
  private List<CostOfGoodsPerDepartmentPerMonth> costPerDepartmentForCurrentMonth;
  private List<PaymentMade> paymentsMadeToday;
  private List<ItemRequest> requestForThisMonth;
}
