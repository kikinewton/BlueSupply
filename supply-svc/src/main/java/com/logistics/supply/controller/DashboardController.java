package com.logistics.supply.controller;

import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.service.DashboardService;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController  {

  private final DashboardService dashboardService;

  @GetMapping("/data")
  public ResponseEntity<?> getDataForDashboard() {
    DashboardData data = dashboardService.getDashboardData();
    if (Objects.nonNull(data)) {
      ResponseDto response = new ResponseDto("FETCH DATA FOR DASHBOARD", Constants.SUCCESS, data);
      return ResponseEntity.ok(response);
    }
    return Helper.failedResponse("FETCH DASHBOARD DATA FAILED");
  }

  @GetMapping("/getAllRequestPerDepartmentForMonth")
  public ResponseEntity<?> requestPerDepartmentForMonth() {
      List<RequestPerCurrentMonthPerDepartment> r = dashboardService.getAllRequestPerDepartmentForMonth();
      if(r.isEmpty()) return Helper.failedResponse("FAILED TO FETCH DATA");
      ResponseDto response =  new ResponseDto("DATA REQUEST PER DEPARTMENT", Constants.SUCCESS, r);
      return ResponseEntity.ok(response);
  }


}
