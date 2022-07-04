package com.logistics.supply.service;

import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.PettyCashPaymentReport;
import com.logistics.supply.repository.PettyCashPaymentReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.logistics.supply.util.Constants.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class PettyCashPaymentReportService {

  private final PettyCashPaymentReportRepository pettyCashPaymentReportRepository;

  public Page<PettyCashPaymentReport> findBetweenDate(
      int pageNo, int pageSize, Date startDate, Date endDate) throws GeneralException {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("payment_date").descending());
      return pettyCashPaymentReportRepository.findByPaymentDateBetween(
          startDate, endDate, pageable);
    } catch (Exception e) {
        e.printStackTrace();
      log.error(e.toString());
    }
    throw new GeneralException(NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public Page<PettyCashPaymentReport> findByRequestedByEmail(
      int pageNo, int pageSize, Date startDate, Date endDate, String requestedByEmail) throws GeneralException {
    try {

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("payment_date").descending());
      return pettyCashPaymentReportRepository.findByPaymentDateBetweenAndRequestedByEmail(
          startDate, endDate, requestedByEmail, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(NOT_FOUND, HttpStatus.NOT_FOUND);
  }
}
