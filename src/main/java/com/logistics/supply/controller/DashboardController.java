package com.logistics.supply.controller;

import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.service.DashboardService;
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
public class DashboardController extends DashboardService {

  @GetMapping("/data")
  public ResponseEntity<?> getDataForDashboard() {
    DashboardData data = getDashboardData();
    if (Objects.nonNull(data)) {
      ResponseDTO response = new ResponseDTO("FETCH_DATA_FOR_DASHBOARD", SUCCESS, data);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_DASHBOARD_DATA_FAILED");
  }

  @GetMapping("/getAllRequestPerDepartmentForMonth")
  public ResponseEntity<?> requestPerDepartmentForMonth() {
    List<RequestPerCurrentMonthPerDepartment> r = new ArrayList<>();
    try {
      r.addAll(getAllRequestPerDepartmentForMonth());
      ResponseDTO response =  new ResponseDTO("DATA_REQUEST_PER_DEPARTMENT", SUCCESS, r);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return failedResponse("FAILED_TO_FETCH_DATA");
  }


}
