package com.logistics.supply.controller;

import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.GeneratedValue;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends DashboardService {

    @GetMapping("/requestPerCurrentMonthPerDepartment")
    public List<RequestPerCurrentMonthPerDepartment> requestPerCurrentMonthPerDepartment() {
        return getAllRequestPerDepartmentForMonth();
    }
}
