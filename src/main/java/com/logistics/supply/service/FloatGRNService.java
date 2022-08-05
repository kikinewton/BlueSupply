package com.logistics.supply.service;

import com.logistics.supply.model.Department;
import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatGRNRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatGRNService {

  private final FloatGRNRepository floatGRNRepository;

  public FloatGRN save(FloatGRN floatGRN) {
      return floatGRNRepository.save(floatGRN);
  }

//  public FloatGRN issueFloatGRN(int floatOrderId, Employee employee) {
//    try {
//      floats.stream()
//          .filter(
//              f ->
//                  f.getStatus().equals(RequestStatus.PROCESSED)
//                      && f.isFundsReceived() == Boolean.TRUE
//                      && f.isProduct())
//          .collect(Collectors.toSet());
//
//      FloatGRN floatGRN = new FloatGRN();
//      floatGRN.setFloats(floats);
//      floatGRN.setCreatedBy(employee);
//      return save(floatGRN);
//    } catch (Exception e) {
//      log.error(e.toString());
//    }
//    return null;
//  }

  public FloatGRN findFloatGRN(Floats floats) {
    return floatGRNRepository.findByFloats(floats).orElse(null);
  }

  public List<FloatGRN> getAllUnApprovedFloatGRN(Department department) {
    try {
      return floatGRNRepository.findFloatGRNPendingApprovalByDepartment(department.getId());
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public FloatGRN findById(long floatGrnId) {
    return floatGRNRepository.findById(floatGrnId).orElse(null);
  }

  public FloatGRN approveByHod(long floatGrnId) {
    return floatGRNRepository
        .findById(floatGrnId)
        .map(
            f -> {
              f.setApprovedByHod(true);
              f.setDateOfApprovalByHod(new Date());
              return floatGRNRepository.save(f);
            })
        .orElse(null);
  }
}
