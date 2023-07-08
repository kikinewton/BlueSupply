package com.logistics.supply.service;

import com.logistics.supply.dto.BulkPettyCashDTO;
import com.logistics.supply.dto.FloatOrPettyCashDto;
import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.event.PettyCashEvent;
import com.logistics.supply.event.listener.FundsReceivedPettyCashListener;
import com.logistics.supply.exception.PettyCashNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.PettyCashOrderRepository;
import com.logistics.supply.repository.PettyCashRepository;
import com.logistics.supply.specification.PettyCashSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import com.logistics.supply.util.IdentifierUtil;
import com.logistics.supply.util.PettyCashValidatorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.RequestStatus.APPROVAL_CANCELLED;
import static com.logistics.supply.enums.RequestStatus.ENDORSEMENT_CANCELLED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PettyCashService {

  private final PettyCashRepository pettyCashRepository;
  private final PettyCashOrderRepository pettyCashOrderRepository;
  private final EmployeeService employeeService;
  private final ApplicationEventPublisher applicationEventPublisher;

  public PettyCash save(PettyCash pettyCash) {

    log.info("Create petty cash");
      return pettyCashRepository.save(pettyCash);
  }

  public List<PettyCash> saveAll(List<PettyCash> cashList) {

      return pettyCashRepository.saveAll(cashList);
  }

  public List<PettyCash> findAllById(List<Integer> ids) {

    log.info("Find Petty cash with ids: {}", ids);
    return pettyCashRepository.findAllById(ids);
  }

  public List<PettyCash> findByDepartment(Department department) {

    log.info("Find petty cash for department: {}", department.getName());
    return pettyCashRepository.findByDepartment(department.getId());
  }

  public Page<PettyCash> findByDepartment(int pageNo, int pageSize, String email) {

    Department department =
            employeeService.findEmployeeByEmail(email).getDepartment();
    log.info("Find petty cash for department: {}", department.getName());
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return pettyCashRepository.findByDepartment(department.getId(), pageable);
  }

  public PettyCashOrder saveAll(FloatOrPettyCashDto bulkItems, Employee employee) {

    log.info("Save petty cash created by: {}", employee.getEmail());
    PettyCashOrder pettyCashOrder = new PettyCashOrder();
    pettyCashOrder.setRequestedBy(bulkItems.getRequestedBy());
    pettyCashOrder.setStaffId(bulkItems.getStaffId());

    pettyCashOrder.setRequestedByPhoneNo(bulkItems.getRequestedByPhoneNo());
    long refCount = countPettyCashOrder();
    String ptcRef = IdentifierUtil.idHandler(
            "PTC",
            employee.getDepartment().getName(),
            String.valueOf(refCount));

    addPettyCashToPettyCashOrder(bulkItems, employee, pettyCashOrder, ptcRef);

    pettyCashOrder.setPettyCashOrderRef(ptcRef);
    PettyCashOrder savedPettyCashOrder = pettyCashOrderRepository.save(pettyCashOrder);
    sendPettyCashOrderEvent(savedPettyCashOrder);
    return savedPettyCashOrder;
  }

  private static void addPettyCashToPettyCashOrder(
          FloatOrPettyCashDto bulkItems,
          Employee employee,
          PettyCashOrder pettyCashOrder,
          String ptcRef) {

    bulkItems.getItems()
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
  }

  private void sendPettyCashOrderEvent(PettyCashOrder pettyCashOrder) {
    if (pettyCashOrder.getPettyCash().isEmpty()) {
      return;
    }
    CompletableFuture.runAsync(
            () -> {
              PettyCashEvent pettyCashEvent = new PettyCashEvent(this, pettyCashOrder.getPettyCash());
              applicationEventPublisher.publishEvent(pettyCashEvent);
            });
  }

  public long count() {
    return pettyCashRepository.countAll() + 1;
  }

  public long countPettyCashOrder() {
    return pettyCashOrderRepository.countAll() + 1;
  }


  public Page<PettyCash> findByEmployee(int employeeId, int pageNo, int pageSize) {

    log.info("Find petty cash list for employee id: {}", employeeId);
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return pettyCashRepository.findByEmployee(employeeId, pageable);
  }

  public PettyCash updatePettyCash(int pettyCashId, ItemUpdateDto itemUpdateDTO) {

    log.info("Update petty cash with id: {}", pettyCashId);
    PettyCash pettyCash = findById(pettyCashId);
    if (Objects.requireNonNull(pettyCash.getStatus()) == RequestStatus.COMMENT) {
      if (itemUpdateDTO.getDescription() != null) {
        pettyCash.setName(itemUpdateDTO.getDescription());
      }
      if (itemUpdateDTO.getQuantity() != null) {
        pettyCash.setQuantity(itemUpdateDTO.getQuantity());
      }
      if (itemUpdateDTO.getEstimatedPrice() != null) {
        pettyCash.setAmount(itemUpdateDTO.getEstimatedPrice());
      }
    }
    return pettyCashRepository.save(pettyCash);
  }


  public List<PettyCash> findApprovedPettyCash(int pageNo, int pageSize) {

    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return pettyCashRepository.findApprovedPettyCash(pageable).getContent();
  }

  public List<PettyCash> findEndorsedPettyCash() {

    log.info("Fetch endorsed petty cash");
    return pettyCashRepository.findEndorsedPettyCash();
  }

  public Page<PettyCash> findAllPettyCashPage(int pageNo, int pageSize) {

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return pettyCashRepository.findAll(pageable);

  }


  public PettyCash cancelPettyCash(int pettyCashId, EmployeeRole employeeRole) {
    PettyCash pettyCash = findById(pettyCashId);
    if (employeeRole.equals(EmployeeRole.ROLE_GENERAL_MANAGER)) {
      pettyCash.setStatus(APPROVAL_CANCELLED);
    } else   {
      pettyCash.setStatus(ENDORSEMENT_CANCELLED);
    }
    return pettyCashRepository.save(pettyCash);
  }


  public PettyCash findById(int pettyCashId) {

    log.info("Find petty cash with id: {}", pettyCashId);
    return pettyCashRepository
        .findById(pettyCashId)
        .orElseThrow(() -> new PettyCashNotFoundException(pettyCashId));
  }


  public PettyCash approve(int pettyCashId) {

    log.info("Approve petty cash id: {}", pettyCashId);
    PettyCash pettyCash = findById(pettyCashId);
    PettyCashValidatorUtil.validateApproval(pettyCash);
    pettyCash.setApproval(RequestApproval.APPROVED);
    pettyCash.setApprovalDate(new Date());
    return pettyCashRepository.save(pettyCash);
  }


  public PettyCash endorse(int pettyCashId) {

    log.info("Endorse petty cash id: {}", pettyCashId);
    PettyCash pettyCash = findById(pettyCashId);
    PettyCashValidatorUtil.validateEndorsement(pettyCash);
    pettyCash.setEndorsement(EndorsementStatus.ENDORSED);
    pettyCash.setEndorsementDate(new Date());
    return pettyCashRepository.save(pettyCash);
  }

  public PettyCash cancelByHod(int pettyCashId) {

    log.info("Reject petty cash id: {} by HOD", pettyCashId);
    PettyCash pettyCash = findById(pettyCashId);
    PettyCashValidatorUtil.validateEndorsement(pettyCash);
    pettyCash.setEndorsement(EndorsementStatus.REJECTED);
    pettyCash.setEndorsementDate(new Date());
    return pettyCashRepository.save(pettyCash);
  }

  public PettyCash cancelByGeneralManager(int pettyCashId) {

    log.info("Reject petty cash id: {} by General Manager", pettyCashId);
    PettyCash pettyCash = findById(pettyCashId);
    PettyCashValidatorUtil.validateEndorsement(pettyCash);
    pettyCash.setApproval(RequestApproval.REJECTED);
    pettyCash.setApprovalDate(new Date());
    return pettyCashRepository.save(pettyCash);
  }


  public List<PettyCash> findPettyCashPendingPayment() {

    log.info("Fetch petty cash pending payment");
      PettyCashSpecification specification = new PettyCashSpecification();
      specification.add(
          new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("paid", false, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("deleted", Boolean.FALSE, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("endorsement", EndorsementStatus.ENDORSED, SearchOperation.EQUAL));
      return pettyCashRepository.findAll(specification);
  }

  public Page<PettyCashOrder> findAllPettyCashOrder(int pageNo, int pageSize) {

    log.info("Fetch all petty cash orders");
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return pettyCashOrderRepository.findAll(pageable);
  }

  public Set<PettyCash> bulkEndorse(Set<PettyCash> bulkPettyCash) {

    return bulkPettyCash.stream()
                    .map(pettyCash -> endorse(pettyCash.getId()))
                    .collect(Collectors.toSet());
  }

  public Set<PettyCash> bulkApproval(Set<PettyCash> bulkPettyCash) {
    return bulkPettyCash.stream()
            .map(pettyCash -> approve(pettyCash.getId()))
            .collect(Collectors.toSet());
  }

  public List<PettyCash> allocateFunds(BulkPettyCashDTO bulkPettyCash, String email) {
    List<Integer> ids = new ArrayList<>();
    bulkPettyCash.getPettyCash()
            .forEach(
                    pettyCash -> {
                      PettyCashValidatorUtil.validateFundsAllocation(pettyCash);
                      ids.add(pettyCash.getId());
                    });

    List<PettyCash> pettyCashList =
            new ArrayList<>();
    for (PettyCash p : findAllById(ids)) {
      p.setStatus(RequestStatus.PROCESSED);
      p.setPaid(true);
      pettyCashList.add(p);
    }

    List<PettyCash> updatedPettyCash = saveAll(pettyCashList);
    sendFundAllocatedEvent(updatedPettyCash, email);
    return updatedPettyCash;
  }

  private void sendFundAllocatedEvent(List<PettyCash> updatedPettyCash, String email) {
    CompletableFuture.runAsync(
            () -> {
              Employee employee = employeeService.findEmployeeByEmail(email);
              FundsReceivedPettyCashListener.FundsReceivedPettyCashEvent fundsReceivedPettyCashEvent =
                      new FundsReceivedPettyCashListener.FundsReceivedPettyCashEvent(
                              this, employee, updatedPettyCash);
              applicationEventPublisher.publishEvent(fundsReceivedPettyCashEvent);
            });
  }
}
