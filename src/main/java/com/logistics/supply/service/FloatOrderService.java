package com.logistics.supply.service;

import com.logistics.supply.dto.FloatDTO;
import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.FloatOrderRepository;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.specification.FloatOrderSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.FETCH_FLOAT_FAILED;
import static com.logistics.supply.util.Constants.FLOAT_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@Service
public class FloatOrderService {

  public final FloatsRepository floatsRepository;
  private final FloatOrderRepository floatOrderRepository;

  public Page<FloatOrder> getAllFloatOrders(int pageNo, int pageSize, boolean retiredStatus) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return floatOrderRepository.findByRetired(retiredStatus, pageable);
  }

  public Page<FloatOrder> getAllEmployeeFloatOrder(int pageNo, int pageSize, Employee employee) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return floatOrderRepository.findByCreatedBy(employee, pageable);
  }

  public Page<FloatOrder> findByEmployee(int employeeId, Pageable pageable) {
    return floatOrderRepository.findByCreatedByIdOrderByIdDesc(employeeId, pageable);
  }

  @SneakyThrows
  public FloatOrder findByRef(String floatOrderRef) {
    return floatOrderRepository
        .findByFloatOrderRef(floatOrderRef)
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public FloatOrder addFloatsToOrder(int floatOrderId, Set<FloatDTO> items) {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            o -> {
              Set<Floats> floatItemList = addFloat(items, o);
              floatItemList.forEach(f -> o.addFloat(f));
              return floatOrderRepository.save(o);
            })
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  private Set<Floats> addFloat(Set<FloatDTO> items, FloatOrder o) {
    Set<Floats> floatItemList =
        items.stream()
            .map(
                i -> {
                  Floats fl = new Floats();
                  fl.setDepartment(o.getDepartment());
                  fl.setEstimatedUnitPrice(i.getEstimatedUnitPrice());
                  fl.setItemDescription(i.getItemDescription());
                  fl.setQuantity(i.getQuantity());
                  fl.setFloatOrder(o);
                  fl.setFloatRef(o.getFloatOrderRef());
                  fl.setCreatedBy(o.getCreatedBy());
                  return fl;
                })
            .collect(Collectors.toSet());
    return floatItemList;
  }

  public Page<FloatOrder> findFloatOrderAwaitingFunds(int pageNo, int pageSize)
      throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(
        new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("status", RequestApproval.PENDING, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("retired", Boolean.FALSE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("fundsReceived", Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(FETCH_FLOAT_FAILED, HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrder> findFloatOrderToClose(int pageNo, int pageSize) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria("retired", Boolean.FALSE, SearchOperation.EQUAL));
    specification.add(
        new SearchCriteria("gmRetirementApproval", Boolean.TRUE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(FETCH_FLOAT_FAILED, HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrder> findFloatsByEndorseStatus(
      int pageNo, int pageSize, EndorsementStatus endorsementStatus) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria("endorsement", endorsementStatus, SearchOperation.EQUAL));
    specification.add(
        new SearchCriteria("approval", RequestApproval.PENDING, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(FETCH_FLOAT_FAILED, HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrder> findFloatsByRequestStatus(int pageNo, int pageSize, RequestStatus status)
      throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria("status", status, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(FETCH_FLOAT_FAILED, HttpStatus.NOT_FOUND);
  }

  @SneakyThrows
  public Page<FloatOrder> findFloatsAwaitingFunds(int pageNo, int pageSize) {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(
        new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("status", RequestApproval.PENDING, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("retired", Boolean.FALSE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("fundsReceived", Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  @SneakyThrows
  public Page<FloatOrder> floatsReceivedFundsAndNotRetired(int pageNo, int pageSize) {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria("status", RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("fundsReceived", Boolean.TRUE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("retired", Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public long count() {
    return floatOrderRepository.count() + 1;
  }

  public FloatOrder allocateFundsFloat(int floatOrderId) throws GeneralException {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            f -> {
              f.setFundsReceived(true);
              f.setStatus(RequestStatus.PROCESSED);
              return floatOrderRepository.save(f);
            })
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public FloatOrder closeRetirement(int floatOrderId) throws Exception {
    return floatOrderRepository
        .findById(floatOrderId)
        .filter(i -> i.getGmRetirementApproval())
        .map(
            o -> {
              o.setRetired(true);
              o.setRetirementDate(new Date());
              return floatOrderRepository.save(o);
            })
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public Page<FloatOrder> floatOrderForAuditorRetire(int pageNo, int pageSize)
      throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria("hasDocument", true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("status", RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(
        new SearchCriteria("auditorRetirementApproval", null, SearchOperation.IS_NULL));
    specification.add(new SearchCriteria("gmRetirementApproval", null, SearchOperation.IS_NULL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("FLOAT NOT FOUND", HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrder> floatOrdersForGmRetire(int pageNo, int pageSize) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria("hasDocument", true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("retired", false, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("status", RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("auditorRetirementApproval", true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("gmRetirementApproval", null, SearchOperation.IS_NULL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("FLOAT NOT FOUND", HttpStatus.NOT_FOUND);
  }

  public FloatOrder endorse(int floatOrderId, EndorsementStatus status) throws GeneralException {
    FloatOrder floatOrder =
        floatOrderRepository
            .findById(floatOrderId)
            .orElseThrow(() -> new GeneralException("FLOAT NOT FOUND", HttpStatus.NOT_FOUND));
    floatOrder.setEndorsement(status);
    floatOrder.setEndorsementDate(new Date());

    return floatOrderRepository.save(floatOrder);
  }

  @SneakyThrows
  public FloatOrder approve(int floatId, RequestApproval approval) {
    FloatOrder floatOrder =
        floatOrderRepository
            .findById(floatId)
            .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
    floatOrder.setApproval(approval);
    floatOrder.setApprovalDate(new Date());
    return floatOrderRepository.save(floatOrder);
  }

  public FloatOrder approveRetirement(int floatId, EmployeeRole employeeRole) throws Exception {
    return floatOrderRepository
        .findById(floatId)
        .map(
            f -> {
              if (employeeRole.equals(EmployeeRole.ROLE_AUDITOR)) {
                f.setAuditorRetirementApproval(true);
                f.setAuditorRetirementApprovalDate(new Date());
                return floatOrderRepository.save(f);
              }
              if (employeeRole.equals(EmployeeRole.ROLE_GENERAL_MANAGER)
                  && f.getAuditorRetirementApproval()) {
                f.setGmRetirementApproval(true);
                f.setGmRetirementApprovalDate(new Date());
                return floatOrderRepository.save(f);
              }
              return null;
            })
        .orElseThrow(Exception::new);
  }

  @SneakyThrows
  public FloatOrder cancel(int floatOrderId, EmployeeRole role) {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            order -> {
              switch (role) {
                case ROLE_HOD:
                  order.setEndorsement(EndorsementStatus.REJECTED);
                  order.setStatus(RequestStatus.ENDORSEMENT_CANCELLED);
                  break;
                default:
                  order.setApproval(RequestApproval.REJECTED);
                  order.setStatus(RequestStatus.APPROVAL_CANCELLED);
                  break;
              }
              return floatOrderRepository.save(order);
            })
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public FloatOrder retirementApproval(int floatId, EmployeeRole employeeRole) {
    FloatOrder floatOrder =
        floatOrderRepository
            .findById(floatId)
            .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
    if (!floatOrder.isFundsReceived() && !floatOrder.isRetired())
      throw new GeneralException("FLOAT RETIREMENT FAILED", HttpStatus.FORBIDDEN);
    switch (employeeRole) {
      case ROLE_GENERAL_MANAGER:
        floatOrder.setGmRetirementApproval(true);
        floatOrder.setGmRetirementApprovalDate(new Date());
        floatOrder.setRetired(true);
        return floatOrderRepository.save(floatOrder);
      case ROLE_AUDITOR:
        floatOrder.setAuditorRetirementApproval(true);
        floatOrder.setAuditorRetirementApprovalDate(new Date());
        return floatOrderRepository.save(floatOrder);
    }
    throw new GeneralException("FLOAT RETIREMENT FAILED", HttpStatus.FORBIDDEN);
  }

  /** this service flags float orders that are 2 or more weeks old without being retired */
  @Async
  @Scheduled(fixedDelay = 21600000, initialDelay = 1000)
  public void flagFloatAfter2Weeks() {
    floatOrderRepository.findUnRetiredFloats().stream()
        .forEach(
            f -> {
              if (f.getCreatedDate()
                  .plusDays(14)
                  .isAfter(ChronoLocalDate.from(LocalDateTime.now()))) {
                f.setFlagged(true);
                floatOrderRepository.save(f);
              }
            });
  }

  @SneakyThrows
  public FloatOrder uploadSupportingDoc(int floatOrderId, Set<RequestDocument> documents) {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            f -> {
              f.setHasDocument(true);
              if (f.getSupportingDocument().isEmpty()) {
                f.setSupportingDocument(documents);
              } else {
                Set<RequestDocument> prevDoc = f.getSupportingDocument();
                documents.forEach(d -> prevDoc.add(d));
                f.setSupportingDocument(prevDoc);
              }
              return floatOrderRepository.save(f);
            })
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public FloatOrder findById(int floatOrderId) {
    return floatOrderRepository
        .findById(floatOrderId)
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public FloatOrder updateFloat(int floatOrderId, ItemUpdateDTO updateDTO) {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            o -> {
              if (updateDTO.getEstimatedPrice() != null) {
                o.setAmount(updateDTO.getEstimatedPrice());
              }
              if (updateDTO.getDescription() != null) {
                o.setDescription(updateDTO.getDescription());
              }
              o.setStatus(RequestStatus.PENDING);
              return floatOrderRepository.save(o);
            })
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public Page<FloatOrder> findAllFloatOrder(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return floatOrderRepository.findAll(pageable);
  }

  @SneakyThrows
  public Page<FloatOrder> findByApprovalStatus(int pageNo, int pageSize, RequestApproval approval) {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria("approval", approval, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("fundsReceived", false, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrder> findFloatsByRetiredStatus(int pageNo, int pageSize, Boolean retired) {
    return null;
  }

  @SneakyThrows
  public Page<FloatOrder> findPendingByDepartment(Department department, Pageable pageable) {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    try {
      specification.add(new SearchCriteria("department", department, SearchOperation.EQUAL));
      specification.add(
          new SearchCriteria("endorsement", EndorsementStatus.PENDING, SearchOperation.EQUAL));
      specification.add(
          new SearchCriteria("approval", RequestApproval.PENDING, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("status", RequestStatus.PENDING, SearchOperation.EQUAL));
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(FETCH_FLOAT_FAILED, HttpStatus.BAD_REQUEST);
  }

  @SneakyThrows
  public Page<FloatOrder> findFloatsAwaitingDocument(int pageNo, int pageSize, int employeeId) {
    try {
      FloatOrderSpecification specification = new FloatOrderSpecification();
      specification.add(
          new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("createdBy", employeeId, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("fundsReceived", true, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("hasDocument", false, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("retired", false, SearchOperation.EQUAL));
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("FLOAT NOT FOUND", HttpStatus.NOT_FOUND);
  }
}
