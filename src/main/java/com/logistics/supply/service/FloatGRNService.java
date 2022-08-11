package com.logistics.supply.service;

import com.logistics.supply.dto.BulkFloatsDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatGRNRepository;
import com.logistics.supply.repository.FloatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.FLOAT_GRN_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatGRNService {
  private final FloatGRNRepository floatGRNRepository;
  private final FloatsRepository floatsRepository;

  public FloatGRN save(FloatGRN floatGRN) {
    return floatGRNRepository.save(floatGRN);
  }

  public FloatGRN issueFloatGRN(BulkFloatsDTO bulkFloatsDTO, Employee employee)
      throws GeneralException {
    Set<Floats> floats =
        bulkFloatsDTO.getFloats().stream()
            .map(f -> floatsRepository.findById(f.getId()).get())
            .collect(Collectors.toSet());
    Integer floatOrderId = floats.stream().findAny().get().getFloatOrder().getId();
    try {
      FloatGRN floatGRN = new FloatGRN();
      floatGRN.setFloats(floats);
      floatGRN.setFloatOrderId(floatOrderId);
      floatGRN.setCreatedBy(employee);
      return save(floatGRN);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("ISSUE FLOAT GRN FAILED", HttpStatus.BAD_REQUEST);
  }

  public FloatGRN findFloatGRN(Floats floats) throws GeneralException {
    return floatGRNRepository
        .findByFloats(floats)
        .orElseThrow(() -> new GeneralException(FLOAT_GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public FloatGRN findById(long floatGrnId) throws GeneralException {
    return floatGRNRepository
        .findById(floatGrnId)
        .orElseThrow(() -> new GeneralException(FLOAT_GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public FloatGRN approveByStoreManager(long floatGrnId, int approvedBy) throws GeneralException {
    FloatGRN floatGRN =
        floatGRNRepository
            .findById(floatGrnId)
            .orElseThrow(() -> new GeneralException(FLOAT_GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
    floatGRN.setApprovedByStoreManager(true);
    floatGRN.setDateOfApprovalByStoreManager(new Date());
    floatGRN.setEmployeeStoreManager(approvedBy);
    return floatGRNRepository.save(floatGRN);
  }

  public List<FloatGRN> getAllApprovedFloatGRNForAuditor() {
    return floatGRNRepository.findByApprovedByStoreManagerIsTrueAndNotRetired();
  }
}
