package com.logistics.supply.service;

import com.logistics.supply.dto.FloatDTO;
import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.FloatOrderRepository;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.specification.FloatOrderSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FloatOrderService {

  public final FloatsRepository floatsRepository;
  private final FloatOrderRepository floatOrderRepository;

  public Page<FloatOrder> getAllFloatOrders(int pageNo, int pageSize, boolean retiredStatus) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findByRetired(retiredStatus, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Page<FloatOrder> getAllEmployeeFloatOrder(int pageNo, int pageSize, Employee employee) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findByCreatedBy(employee, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Page<FloatOrder> findByEmployee(int employeeId, Pageable pageable) {
    try {
      return floatOrderRepository.findByCreatedByIdOrderByIdDesc(employeeId, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public FloatOrder findByRef(String floatOrderRef) {
    try {
      return floatOrderRepository.findByFloatOrderRef(floatOrderRef).orElse(null);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public FloatOrder addFloatsToOrder(int floatOrderId, Set<FloatDTO> items) {
    try {
      return floatOrderRepository
          .findById(floatOrderId)
          .filter(f -> f.isFundsReceived())
          .map(
              o -> {
                Set<Floats> floatItemList = addFloat(items, o);
                floatItemList.forEach(f -> o.addFloat(f));
                return floatOrderRepository.save(o);
              })
          .orElse(null);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
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

  public Page<FloatOrder> findFloatOrderAwaitingFunds(int pageNo, int pageSize) {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(
        new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("status", RequestApproval.PENDING, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("retired", Boolean.FALSE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("funds_received", Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<FloatOrder> findFloatsByEndorseStatus(
      int pageNo, int pageSize, EndorsementStatus endorsementStatus) {
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
    return null;
  }

  public Page<FloatOrder> findFloatsByRequestStatus(
      int pageNo, int pageSize, RequestStatus status) {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria("status", status, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<FloatOrder> findFloatsAwaitingFunds(int pageNo, int pageSize) {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(
        new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("status", RequestApproval.PENDING, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("retired", Boolean.FALSE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("funds_received", Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

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
    return null;
  }

  public long count() {
    return floatOrderRepository.count() + 1;
  }

  public FloatOrder allocateFundsFloat(int floatOrderId) {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            f -> {
              f.setFundsReceived(true);
              f.setStatus(RequestStatus.PROCESSED);
              return floatOrderRepository.save(f);
            })
        .orElse(null);
  }

  public Page<FloatOrder> floatOrderForAuditorRetire(int pageNo, int pageSize) {
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
    return null;
  }

  public Page<FloatOrder> floatOrdersForGmRetire(int pageNo, int pageSize) {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria("has_document", true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("status", RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(
        new SearchCriteria("auditor_retirement_approval", true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("gm_retirement_approval", false, SearchOperation.IS_NULL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public FloatOrder endorse(int floatOrderId, EndorsementStatus status) {
    Optional<FloatOrder> f = floatOrderRepository.findById(floatOrderId);
    if (f.isPresent()) {
      FloatOrder o = f.get();
      o.setEndorsement(status);
      o.setEndorsementDate(new Date());
      try {
        return floatOrderRepository.save(o);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
    return null;
  }

  public FloatOrder approve(int floatId, RequestApproval approval) {
    return floatOrderRepository
        .findById(floatId)
        .map(
            f -> {
              f.setApproval(approval);
              f.setApprovalDate(new Date());
              return floatOrderRepository.save(f);
            })
        .orElse(null);
  }

  public FloatOrder approveRetirement(int floatId, EmployeeRole employeeRole) throws Exception {
    return floatOrderRepository
        .findById(floatId)
        .map(
            f -> {
              switch (employeeRole) {
                case ROLE_AUDITOR:
                  f.setAuditorRetirementApproval(true);
                  f.setAuditorRetirementApprovalDate(new Date());
                case ROLE_GENERAL_MANAGER:
                  if (f.getAuditorRetirementApproval()) {
                    f.setGmRetirementApproval(true);
                    f.setGmRetirementApprovalDate(new Date());
                    f.setRetirementDate(new Date());
                  }
              }
              return floatOrderRepository.save(f);
            })
        .orElseThrow(Exception::new);
  }

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
        .orElse(null);
  }

  public FloatOrder retirementApproval(int floatId, EmployeeRole employeeRole) {
    return floatOrderRepository
        .findById(floatId)
        .filter(f -> !f.isRetired() && !f.isFundsReceived())
        .map(
            i -> {
              switch (employeeRole) {
                case ROLE_GENERAL_MANAGER:
                  i.setGmRetirementApproval(true);
                  i.setGmRetirementApprovalDate(new Date());
                  i.setRetired(true);
                  return floatOrderRepository.save(i);
                case ROLE_AUDITOR:
                  i.setAuditorRetirementApproval(true);
                  i.setAuditorRetirementApprovalDate(new Date());
                  return floatOrderRepository.save(i);
              }
              return null;
            })
        .orElse(null);
  }

  /** this service flags float orders that are 2 or more weeks old without being retired */
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
        .orElse(null);
  }

  public FloatOrder findById(int floatOrderId) {
    return floatOrderRepository.findById(floatOrderId).orElse(null);
  }

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
              return floatOrderRepository.save(o);
            })
        .orElse(null);
  }

  public Page<FloatOrder> findAllFloatOrder(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return floatOrderRepository.findAll(pageable);
  }

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
    return null;
  }

  public Page<FloatOrder> findFloatsByRetiredStatus(int pageNo, int pageSize, Boolean retired) {
    return null;
  }

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
    return null;
  }

  public Page<FloatOrder> findFloatsAwaitingDocument(int pageNo, int pageSize) {
    try {
      FloatOrderSpecification specification = new FloatOrderSpecification();
      specification.add(
          new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("fundsReceived", true, SearchOperation.EQUAL));
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }
}
