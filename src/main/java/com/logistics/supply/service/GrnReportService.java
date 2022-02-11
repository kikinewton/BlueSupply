package com.logistics.supply.service;

import com.logistics.supply.model.GrnReport;
import com.logistics.supply.repository.GrnReportRepository;
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
public class GrnReportService {
  private final GrnReportRepository grnReportRepository;

  public Page<GrnReport> findBetweenDate(int pageNo, int pageSize, Date startDate, Date endDate) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return grnReportRepository.findByDateReceivedBetween(startDate, endDate, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Page<GrnReport> findBySupplier(
      int pageNo, int pageSize, Date startDate, Date endDate, String supplier) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return grnReportRepository.findByDateReceivedBetweenAndSupplierIgnoreCase(
          startDate, endDate, supplier, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }
}
