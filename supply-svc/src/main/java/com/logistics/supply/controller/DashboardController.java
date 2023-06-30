package com.logistics.supply.controller;

import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController  {

  private final DashboardService dashboardService;

  @GetMapping("/data")
  public ResponseEntity<ResponseDto<DashboardData>> getDataForDashboard() {

    DashboardData data = dashboardService.getDashboardData();
      return ResponseDto.wrapSuccessResult(data, "FETCH DATA FOR DASHBOARD");
  }

  @GetMapping("/getAllRequestPerDepartmentForMonth")
  public ResponseEntity<ResponseDto<List<RequestPerCurrentMonthPerDepartment>>> requestPerDepartmentForMonth() {

      List<RequestPerCurrentMonthPerDepartment> allRequestPerDepartmentForMonth = dashboardService.getAllRequestPerDepartmentForMonth();
       return ResponseDto.wrapSuccessResult(allRequestPerDepartmentForMonth, "DATA REQUEST PER DEPARTMENT");

  }


}
