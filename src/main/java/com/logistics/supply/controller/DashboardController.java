package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.service.DashboardService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends DashboardService {

    @GetMapping("/requestPerCurrentMonthPerDepartment")
    public ResponseDTO<List<RequestPerCurrentMonthPerDepartment>> requestPerCurrentMonthPerDepartment() {
        List<RequestPerCurrentMonthPerDepartment> requests = getAllRequestPerDepartmentForMonth();
        return ResponseDTO<Lis<RequestPerCurrentMonthPerDepartment>>(HttpStatus.OK.name(), requests, "FOUND");
    }
}
