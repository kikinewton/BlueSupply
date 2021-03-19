package com.logistics.supply.controller;

import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends DashboardService {

    @GetMapping("/requestPerCurrentMonthPerDepartment")
    public List<RequestPerCurrentMonthPerDepartment> requestPerCurrentMonthPerDepartment() {
        return getAllRequestPerDepartmentForMonth();
    }
}
