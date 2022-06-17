package com.logistics.supply.service;

import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.repository.PettyCashRepository;
import com.logistics.supply.specification.PettyCashSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.logistics.supply.enums.RequestStatus.APPROVAL_CANCELLED;
import static com.logistics.supply.enums.RequestStatus.ENDORSEMENT_CANCELLED;
import static com.logistics.supply.util.Constants.PETTY_CASH_NOT_FOUND;

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

  public List<PettyCash> saveAll(List<PettyCash> cashList) {
    try {
      return pettyCashRepository.saveAll(cashList);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public List<PettyCash> findAllById(List<Integer> ids) {
    return pettyCashRepository.findAllById(ids);
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
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
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
        .filter(i -> i.getStatus().equals(RequestStatus.COMMENT))
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

  @SneakyThrows
  public List<PettyCash> findApprovedPettyCash(int pageNo, int pageSize) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return pettyCashRepository.findApprovedPettyCash(pageable).getContent();
    } catch (Exception e) {
      log.error(e.toString());
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
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

  @SneakyThrows
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
        .orElseThrow(() -> new GeneralException(PETTY_CASH_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public PettyCash findById(int pettyCashId) {
    return pettyCashRepository
        .findById(pettyCashId)
        .orElseThrow(() -> new GeneralException("Petty cash not found", HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public PettyCash approve(int pettyCashId, RequestApproval approval) {
    return pettyCashRepository
        .findById(pettyCashId)
        .map(
            f -> {
              f.setApproval(approval);
              f.setApprovalDate(new Date());
              return pettyCashRepository.save(f);
            })
        .orElseThrow(() -> new GeneralException("Petty cash not found", HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public PettyCash endorse(int pettyCashId, EndorsementStatus status) {
    return pettyCashRepository
        .findById(pettyCashId)
        .map(
            f -> {
              f.setEndorsement(status);
              f.setEndorsementDate(new Date());
              return pettyCashRepository.save(f);
            })
        .orElseThrow(() -> new GeneralException("Petty cash not found", HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public List<PettyCash> findPettyCashPendingPayment() {
    try {
      PettyCashSpecification specification = new PettyCashSpecification();
      specification.add(
          new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("paid", null, SearchOperation.IS_NULL));
      specification.add(new SearchCriteria("status", RequestStatus.PENDING, SearchOperation.EQUAL));
      return pettyCashRepository.findAll(specification);
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }
}
