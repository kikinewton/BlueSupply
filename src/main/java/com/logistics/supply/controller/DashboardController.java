package com.logistics.supply.controller;

import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController  {

  private final DashboardService dashboardService;

  @GetMapping("/data")
  public ResponseEntity<?> getDataForDashboard() {
    DashboardData data = dashboardService.getDashboardData();
    if (Objects.nonNull(data)) {
      ResponseDTO response = new ResponseDTO("FETCH DATA FOR DASHBOARD", SUCCESS, data);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH DASHBOARD DATA FAILED");
  }

  @GetMapping("/getAllRequestPerDepartmentForMonth")
  public ResponseEntity<?> requestPerDepartmentForMonth() {
      List<RequestPerCurrentMonthPerDepartment> r = dashboardService.getAllRequestPerDepartmentForMonth();
      if(r.isEmpty()) return failedResponse("FAILED TO FETCH DATA");
      ResponseDTO response =  new ResponseDTO("DATA REQUEST PER DEPARTMENT", SUCCESS, r);
      return ResponseEntity.ok(response);
  }


}
