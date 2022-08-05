package com.logistics.supply.service;

import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.FloatAgingAnalysis;
import com.logistics.supply.repository.FloatAgingAnalysisRepository;
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
public class FloatAgeingAnalysisService {
  private final FloatAgingAnalysisRepository floatAgingAnalysisRepository;
  public Page<FloatAgingAnalysis> findAllFloatAnalysis(int pageNo, int pageSize) throws GeneralException {
    try {
      //not using native query thus createdDate from model
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
      return floatAgingAnalysisRepository.findAll(pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public Page<FloatAgingAnalysis> findBetweenDate(
      int pageNo, int pageSize, Date startDate, Date endDate) throws GeneralException {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("created_date").descending());
      return floatAgingAnalysisRepository.findAllBBetweenDate(startDate, endDate, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public Page<FloatAgingAnalysis> findFloatAnalysisByRequesterEmail(
      int pageNo, int pageSize, String requestedEmail) throws GeneralException {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("created_date").descending());
      return floatAgingAnalysisRepository.findByRequestedByEmail(requestedEmail.toUpperCase(), pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public Page<FloatAgingAnalysis> findFloatAnalysisByStaffId(
          int pageNo, int pageSize, String staffId) throws GeneralException {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("created_date").descending());
      return floatAgingAnalysisRepository.findByStaffId(staffId.toUpperCase(), pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(NOT_FOUND, HttpStatus.NOT_FOUND);
  }
}
