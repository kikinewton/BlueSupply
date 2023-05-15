package com.logistics.supply.service;

import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.FloatOrderPaymentReport;
import com.logistics.supply.repository.FloatOrderPaymentReportRepository;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatOrderPaymentReportService {
  private final FloatOrderPaymentReportRepository floatOrderPaymentReportRepository;

  public Page<FloatOrderPaymentReport> findByRequesterStaffId(String staffId, Pageable pageable)
      throws GeneralException {
    try {
      return floatOrderPaymentReportRepository.findByRequesterStaffIdEqualsIgnoreCase(
          staffId, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrderPaymentReport> findAll(Pageable pageable){
    return floatOrderPaymentReportRepository.findAll(pageable);
  }

  public Page<FloatOrderPaymentReport> findBetweenFundsPaidDate(
      Date startDate, Date endDate, Pageable pageable) throws GeneralException {
    try {
      return floatOrderPaymentReportRepository.findByFundsAllocatedDateBetween(
          startDate, endDate, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.NOT_FOUND, HttpStatus.NOT_FOUND);
  }
}
