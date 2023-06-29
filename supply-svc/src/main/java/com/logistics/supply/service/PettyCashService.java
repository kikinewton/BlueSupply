package com.logistics.supply.service;

import com.logistics.supply.dto.FloatOrPettyCashDto;
import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.PettyCashEvent;
import com.logistics.supply.exception.PettyCashNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.PettyCashOrderRepository;
import com.logistics.supply.repository.PettyCashRepository;
import com.logistics.supply.specification.PettyCashSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.logistics.supply.enums.RequestStatus.APPROVAL_CANCELLED;
import static com.logistics.supply.enums.RequestStatus.ENDORSEMENT_CANCELLED;
import static com.logistics.supply.util.Constants.PETTY_CASH_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class PettyCashService {
  private final PettyCashRepository pettyCashRepository;
  private final PettyCashOrderRepository pettyCashOrderRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  public PettyCash save(PettyCash pettyCash) {
      return pettyCashRepository.save(pettyCash);
  }

  public List<PettyCash> saveAll(List<PettyCash> cashList) throws GeneralException {
    try {
      return pettyCashRepository.saveAll(cashList);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("SAVE PETTY CASH FAILED", HttpStatus.BAD_REQUEST);
  }

  public List<PettyCash> findAllById(List<Integer> ids) {
    return pettyCashRepository.findAllById(ids);
  }

  public List<PettyCash> findByDepartment(Department department) {
    return pettyCashRepository.findByDepartment(department.getId());
  }

  public PettyCashOrder saveAll(FloatOrPettyCashDto bulkItems, Employee employee) {
    PettyCashOrder pettyCashOrder = new PettyCashOrder();
    pettyCashOrder.setRequestedBy(bulkItems.getRequestedBy());
    pettyCashOrder.setStaffId(bulkItems.getStaffId());

    pettyCashOrder.setRequestedByPhoneNo(bulkItems.getRequestedByPhoneNo());
    long refCount = countPtcOrder();
    String ptcRef = IdentifierUtil.idHandler(
            "PTC",
            employee.getDepartment().getName(),
            String.valueOf(refCount));

    bulkItems.getItems().stream()
            .forEach(
                    i -> {
                      PettyCash pettyCash = new PettyCash();
                      pettyCash.setDepartment(employee.getDepartment());
                      pettyCash.setName(i.getName());
                      pettyCash.setPurpose(i.getPurpose());
                      pettyCash.setAmount(i.getUnitPrice());
                      pettyCash.setQuantity(i.getQuantity());
                      pettyCash.setStaffId(bulkItems.getStaffId());
                      pettyCash.setCreatedBy(employee);

                      pettyCash.setPettyCashRef(ptcRef);
                      pettyCashOrder.addPettyCash(pettyCash);
                    });
    pettyCashOrder.setPettyCashOrderRef(ptcRef);
    PettyCashOrder saved = pettyCashOrderRepository.save(pettyCashOrder);
    if (!saved.getPettyCash().isEmpty()) {
      PettyCashOrder finalSaved = saved;
      CompletableFuture.runAsync(
              () -> {
                PettyCashEvent pettyCashEvent = new PettyCashEvent(this, finalSaved.getPettyCash());
                applicationEventPublisher.publishEvent(pettyCashEvent);
              });
    }
    return saved;

  }

  public long count() {
    return pettyCashRepository.countAll() + 1;
  }

  public long countPtcOrder() {
    return pettyCashOrderRepository.countAll() + 1;
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

  public PettyCash updatePettyCash(int pettyCashId, ItemUpdateDto itemUpdateDTO)
      throws GeneralException {
    PettyCash pettyCash =
        pettyCashRepository
            .findById(pettyCashId)
            .orElseThrow(() -> new GeneralException(PETTY_CASH_NOT_FOUND, HttpStatus.NOT_FOUND));
    if (RequestStatus.COMMENT.equals(pettyCash.getStatus())) {
      if (itemUpdateDTO.getDescription() != null) pettyCash.setName(itemUpdateDTO.getDescription());
      if (itemUpdateDTO.getQuantity() != null) pettyCash.setQuantity(itemUpdateDTO.getQuantity());
      if (itemUpdateDTO.getEstimatedPrice() != null)
        pettyCash.setAmount(itemUpdateDTO.getEstimatedPrice());
      return pettyCashRepository.save(pettyCash);
    }
    throw new GeneralException("UPDATE PETTY CASH FAILED", HttpStatus.NOT_FOUND);
  }

  @SneakyThrows
  public List<PettyCash> findApprovedPettyCash(int pageNo, int pageSize) {

    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return pettyCashRepository.findApprovedPettyCash(pageable).getContent();
  }

  public List<PettyCash> findEndorsedPettyCash() {
    return pettyCashRepository.findEndorsedPettyCash();
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

  public Page<PettyCash> findAllPettyCashPage(int pageNo, int pageSize) {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return pettyCashRepository.findAll(pageable);

  }

  @SneakyThrows
  public PettyCash cancelPettyCash(int pettyCashId, EmployeeRole employeeRole) {
    PettyCash pettyCash =
        pettyCashRepository
            .findById(pettyCashId)
            .orElseThrow(() -> new GeneralException(PETTY_CASH_NOT_FOUND, HttpStatus.NOT_FOUND));
    if (employeeRole.equals(EmployeeRole.ROLE_GENERAL_MANAGER)) {
      pettyCash.setStatus(APPROVAL_CANCELLED);
      return pettyCashRepository.save(pettyCash);
    } else if (employeeRole.equals(EmployeeRole.ROLE_HOD)) {
      pettyCash.setStatus(ENDORSEMENT_CANCELLED);
      return pettyCashRepository.save(pettyCash);
    }
    throw new GeneralException("CANCEL PETTY CASH FAILED", HttpStatus.BAD_REQUEST);
  }

  @SneakyThrows
  public PettyCash findById(int pettyCashId) {
    return pettyCashRepository
        .findById(pettyCashId)
        .orElseThrow(() -> new GeneralException(PETTY_CASH_NOT_FOUND, HttpStatus.NOT_FOUND));
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
        .orElseThrow(() -> new PettyCashNotFoundException(pettyCashId));
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
        .orElseThrow(() -> new PettyCashNotFoundException(pettyCashId));
  }

  @SneakyThrows
  public List<PettyCash> findPettyCashPendingPayment() {
    try {
      PettyCashSpecification specification = new PettyCashSpecification();
      specification.add(
          new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("paid", false, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("deleted", Boolean.FALSE, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("endorsement", EndorsementStatus.ENDORSED, SearchOperation.EQUAL));
      return pettyCashRepository.findAll(specification);
    } catch (Exception e) {
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public Page<PettyCashOrder> findAllPettyCashOrder(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return pettyCashOrderRepository.findAll(pageable);
  }
}
