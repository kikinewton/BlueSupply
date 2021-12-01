package com.logistics.supply.service;

import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.repository.PettyCashRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.logistics.supply.enums.RequestStatus.APPROVAL_CANCELLED;
import static com.logistics.supply.enums.RequestStatus.ENDORSEMENT_CANCELLED;

@Slf4j
@Service
public class PettyCashService {

  @Autowired PettyCashRepository pettyCashRepository;

  public PettyCash save(PettyCash pettyCash) {
    try {
      return pettyCashRepository.save(pettyCash);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public List<PettyCash> findByDepartment(Department department) {
    try {
      return pettyCashRepository.findByDepartment(department.getId());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ArrayList<>();
  }

  public PettyCash findByRef(String pettyCashRef) {
    Optional<PettyCash> result = pettyCashRepository.findByPettyCashRef(pettyCashRef);
    if (result.isPresent()) {
      return result.get();
    }
    return null;
  }

  public long count() {
    return pettyCashRepository.count() + 1;
  }

  public List<PettyCash> findByEmployee(int employeeId, int pageNo, int pageSize) {
    List<PettyCash> cashList = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("created_date").descending());
      cashList.addAll(pettyCashRepository.findByEmployee(employeeId, pageable).getContent());
      return cashList;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return cashList;
  }

  public PettyCash updatePettyCash(int pettyCashId, ItemUpdateDTO itemUpdateDTO) {
    return pettyCashRepository
        .findById(pettyCashId)
        .filter(i -> i.getStatus() == RequestStatus.COMMENT)
        .map(
            p -> {
              if (itemUpdateDTO.getDescription() != null) p.setName(itemUpdateDTO.getDescription());
              if (itemUpdateDTO.getQuantity() != null) p.setQuantity(itemUpdateDTO.getQuantity());
              if (itemUpdateDTO.getEstimatedPrice() != null)
                p.setAmount(itemUpdateDTO.getEstimatedPrice());
              return pettyCashRepository.save(p);
            })
        .orElse(null);
  }

  public List<PettyCash> findApprovedPettyCash(int pageNo, int pageSize) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return pettyCashRepository.findApprovedPettyCash(pageable).getContent();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public List<PettyCash> findEndorsedPettyCash() {
    try {
      return pettyCashRepository.findEndorsedPettyCash();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public List<PettyCash> findAllPettyCash(int pageNo, int pageSize) {
    List<PettyCash> cashList = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      Page<PettyCash> pettyCashPage = pettyCashRepository.findAll(pageable);
      cashList.addAll(pettyCashPage.getContent());
      return cashList;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return cashList;
  }

  public PettyCash cancelPettyCash(int pettyCashId, EmployeeRole employeeRole) {
    return pettyCashRepository
        .findById(pettyCashId)
        .map(
            r -> {
              if (employeeRole.equals(EmployeeRole.ROLE_GENERAL_MANAGER)) {
                r.setStatus(APPROVAL_CANCELLED);
                return pettyCashRepository.save(r);
              } else if (employeeRole.equals(EmployeeRole.ROLE_HOD)) {
                r.setStatus(ENDORSEMENT_CANCELLED);
                return pettyCashRepository.save(r);
              }
              return null;
            })
        .orElse(null);
  }

  public PettyCash findById(int pettyCashId) {
    return pettyCashRepository.findById(pettyCashId).orElse(null);
  }
}
