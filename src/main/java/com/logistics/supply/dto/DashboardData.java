package com.logistics.supply.dto;

import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class DashboardData {

    public DashboardData() {
    }

    int countOfPaymentDueWithinOneWeek;
    int countOfGRNForToday;
    int countPaymentsMadeToday;
    int countOfRequestPerCurrentMonth;
    List<RequestPerUserDepartment> approvedNumberRequestItemsAndUserDepartmentToday;
    List<RequestPerCategory> requestPerCategoryForToday;
    List<CostOfGoodsPerDepartmentPerMonth> costPerDepartmentForCurrentMonth;
    List<SpendAnalysisDTO> supplierSpendAnalysis;
    List<RequestPerCurrentMonthPerDepartment> requestsPerCurrentMonthPerDepartment;
}
