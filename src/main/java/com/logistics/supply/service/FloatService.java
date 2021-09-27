package com.logistics.supply.service;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatService {

  private final FloatsRepository floatsRepository;

  public List<Floats> findAllFloats(int pageNo, int pageSize) {
    List<Floats> floats = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
      floats.addAll(floatsRepository.findAll(pageable).getContent());
      return floats;
    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    return floats;
  }

  public Floats saveFloat(Floats floats) {
    try {
      return floatsRepository.save(floats);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Floats endorse(int floatId, EndorsementStatus status) {
    return floatsRepository
        .findById(floatId)
        .map(
            f -> {
              f.setEndorsement(status);
              return floatsRepository.save(f);
            })
        .orElse(null);
  }

  public Floats approve(int floatId, RequestApproval approval) {
    return floatsRepository
        .findById(floatId)
        .map(
            f -> {
              f.setApproval(approval);
              return floatsRepository.save(f);
            })
        .orElse(null);
  }

  public Floats findByRef(String floatRef) {
    try {
      return floatsRepository.findByFloatRef(floatRef).orElse(null);
    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    return null;
  }

  public List<Floats> findByDepartment(Department department) {
    List<Floats> floats = new ArrayList<>();
    try {
      floats.addAll(floatsRepository.findByDepartment(department));
      return floats;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return floats;
  }

    public List<Floats> findByEmployee(int employeeId, int pageNo, int pageSize) {
      List<Floats> floats = new ArrayList<>();
      try {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
        floats.addAll(floatsRepository.findByEmployeeId(employeeId, pageable).getContent());
        return floats;
      } catch (Exception e) {
        log.error(e.toString());
      }
      return floats;
    }

  public void flagFloatAfter2Weeks() {
    // todo create a service to flag floats that are 2 or more weeks old
  }
}
