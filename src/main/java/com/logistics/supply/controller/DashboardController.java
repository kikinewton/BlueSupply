package com.logistics.supply.controller;

import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.service.DashboardService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends DashboardService {

  @GetMapping("/data")
  public ResponseDTO<DashboardData> getDataForDashboard() {
    DashboardData data = getDashboardData();
    if (Objects.nonNull(data)) return new ResponseDTO<>(HttpStatus.OK.name(), data, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }
}
