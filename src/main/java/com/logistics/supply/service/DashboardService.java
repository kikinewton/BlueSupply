package com.logistics.supply.service;

import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.repository.RequestPerMonthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DashboardService {

    @Autowired public RequestPerMonthRepository requestPerMonthRepository;

    public List<RequestPerCurrentMonthPerDepartment> getAllRequestPerDepartmentForMonth() {
        List<RequestPerCurrentMonthPerDepartment> requests = requestPerMonthRepository.findAll();
        return requests;
    }
}
