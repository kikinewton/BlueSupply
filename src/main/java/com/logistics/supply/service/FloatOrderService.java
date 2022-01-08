package com.logistics.supply.service;

import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.FloatOrderRepository;
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
import java.util.Date;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class FloatOrderService {

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

  public FloatOrder addFloatsToOrder(String floatOrderRef, Set<Floats> floats) {
    try {
      floatOrderRepository
          .findByFloatOrderRef(floatOrderRef)
          .map(
              o -> {
                floats.forEach(f -> o.addFloat(f));
                return floatOrderRepository.save(o);
              })
          .orElse(null);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
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
    specification.add(new SearchCriteria("funds_received", Boolean.TRUE, SearchOperation.EQUAL));
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

  public FloatOrder endorse(int floatOrderId, EndorsementStatus status) {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            f -> {
              f.setEndorsement(status);
              f.setEndorsementDate(new Date());
              return floatOrderRepository.save(f);
            })
        .orElse(null);
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
              if (f.getCreatedDate().get().plusDays(14).isAfter(LocalDateTime.now())) {
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
              f.setSupportingDocument(documents);
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
}
