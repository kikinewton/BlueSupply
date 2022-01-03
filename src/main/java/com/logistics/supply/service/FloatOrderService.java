package com.logistics.supply.service;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.repository.FloatOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FloatOrderService {

  private final FloatOrderRepository floatOrderRepository;

  public Page<FloatOrder> getAllFloatOrders(int pageNo, int pageSize, boolean retiredStatus) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findByRetired(retiredStatus, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Page<FloatOrder> getAllEmployeeFloatOrder(int pageNo, int pageSize, Employee employee) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findByCreatedBy(employee, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }
}
