package com.logistics.supply.service;

import com.logistics.supply.model.PettyCashPaymentReport;
import com.logistics.supply.repository.PettyCashPaymentReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class PettyCashPaymentReportService {

  private final PettyCashPaymentReportRepository pettyCashPaymentReportRepository;

  public Page<PettyCashPaymentReport> findBetweenDate(
      int pageNo, int pageSize, Date startDate, Date endDate) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("payment_date").descending());
      return pettyCashPaymentReportRepository.findByPaymentDateBetween(
          startDate, endDate, pageable);
    } catch (Exception e) {
        e.printStackTrace();
      log.error(e.toString());
    }
    return null;
  }

  public Page<PettyCashPaymentReport> findByRequestedByEmail(
      int pageNo, int pageSize, Date startDate, Date endDate, String requestedByEmail) {
    try {
      System.out.println("pageNo = " + 2);
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("payment_date").descending());
      return pettyCashPaymentReportRepository.findByPaymentDateBetweenAndRequestedByEmail(
          startDate, endDate, requestedByEmail, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }
}
