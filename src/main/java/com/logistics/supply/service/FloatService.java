package com.logistics.supply.service;

import com.google.common.collect.ImmutableSet;
import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.Floats;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.specification.FloatSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import static com.logistics.supply.enums.RequestStatus.APPROVAL_CANCELLED;
import static com.logistics.supply.enums.RequestStatus.ENDORSEMENT_CANCELLED;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatService {

  private final FloatsRepository floatsRepository;

  public Page<Floats> findAllFloats(int pageNo, int pageSize) {

    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
      Page<Floats> floats = floatsRepository.findAll(pageable);
      return floats;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Page<Floats> findByApprovalStatus(
      int pageNo, int pageSize, RequestApproval requestApproval) {
    FloatSpecification specification = new FloatSpecification();
    specification.add(new SearchCriteria("approval", requestApproval, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<Floats> findFloatsByRetiredStatus(int pageNo, int pageSize, boolean retired) {
    FloatSpecification specification = new FloatSpecification();
    specification.add(new SearchCriteria("retired", retired, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<Floats> floatsWithoutGRN(int pageNo, int pageSize) {
    FloatSpecification specification = new FloatSpecification();
    specification.add(new SearchCriteria("status", RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("funds_received", Boolean.TRUE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("is_product", Boolean.TRUE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("retired", Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<Floats> floatsReceivedFundsAndNotRetired(int pageNo, int pageSize) {
    FloatSpecification specification = new FloatSpecification();
    specification.add(new SearchCriteria("status", RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("funds_received", Boolean.TRUE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("retired", Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<Floats> findFloatsAwaitingFunds(int pageNo, int pageSize) {
    FloatSpecification specification = new FloatSpecification();
    specification.add(
        new SearchCriteria("approval", RequestApproval.APPROVED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("status", RequestApproval.PENDING, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("retired", Boolean.FALSE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("funds_received", Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<Floats> findFloatsByEndorseStatus(
      int pageNo, int pageSize, EndorsementStatus endorsementStatus) {
    FloatSpecification specification = new FloatSpecification();
    specification.add(new SearchCriteria("endorsement", endorsementStatus, SearchOperation.EQUAL));
    specification.add(
        new SearchCriteria("approval", RequestApproval.PENDING, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<Floats> findFloatsByRequestStatus(int pageNo, int pageSize, RequestStatus status) {
    FloatSpecification specification = new FloatSpecification();
    specification.add(new SearchCriteria("status", status, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public long count() {
    return floatsRepository.count() + 1;
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
              f.setEndorsementDate(new Date());
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
              f.setApprovalDate(new Date());
              return floatsRepository.save(f);
            })
        .orElse(null);
  }

  public Floats findByRef(String floatRef) {
    try {
      return floatsRepository.findByFloatRef(floatRef).orElse(null);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Page<Floats> findPendingByDepartment(Department department, Pageable pageable) {
    try {
      return floatsRepository.findByDepartmentAndEndorsementOrderByIdDesc(
          department, EndorsementStatus.PENDING, pageable);

    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Page<Floats> findByStatusAndDepartment(
      FloatSpecification specification, Pageable pageable) {
    try {
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<Floats> findByEmployee(int employeeId, Pageable pageable) {
    try {
      return floatsRepository.findByCreatedByIdOrderByIdDesc(employeeId, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Page<Floats> findAll(Pageable pageable) {
    return floatsRepository.findAll(pageable);
  }

  public Floats updateFloat(int floatId, ItemUpdateDTO itemUpdate) {
    return floatsRepository
        .findById(floatId)
        .filter(i -> i.getStatus() == RequestStatus.COMMENT)
        .map(
            f -> {
              if (itemUpdate.getQuantity() != null) f.setQuantity(itemUpdate.getQuantity());
              if (itemUpdate.getDescription() != null)
                f.setItemDescription(itemUpdate.getDescription());
              if (itemUpdate.getEstimatedPrice() != null)
                f.setEstimatedUnitPrice(itemUpdate.getEstimatedPrice());
              f.setStatus(RequestStatus.PENDING);
              return floatsRepository.save(f);
            })
        .orElse(null);
  }

  @Scheduled(fixedDelay = 21600000, initialDelay = 1000)
  public void flagFloatAfter2Weeks() {
    // todo create a service to flag floats that are 2 or more weeks old
    floatsRepository.findUnRetiredFloats().stream()
        .forEach(
            f -> {
              if (f.getCreatedDate()
                  .toInstant()
                  .atZone(ZoneId.systemDefault())
                  .toLocalDateTime()
                  .plusDays(14)
                  .isAfter(LocalDateTime.now())) {
                f.setFlagged(true);
                floatsRepository.save(f);
              }
            });
  }

  public Floats cancelFloat(int floatId, EmployeeRole employeeRole) {
    return floatsRepository
        .findById(floatId)
        .map(
            r -> {
              if (employeeRole.equals(EmployeeRole.ROLE_GENERAL_MANAGER)) {
                r.setStatus(APPROVAL_CANCELLED);
                return floatsRepository.save(r);
              } else if (employeeRole.equals(EmployeeRole.ROLE_HOD)) {
                r.setStatus(ENDORSEMENT_CANCELLED);
                return floatsRepository.save(r);
              }
              return null;
            })
        .orElse(null);
  }

  public Floats uploadSupportingDoc(int floatId, RequestDocument document) {
    return floatsRepository
        .findById(floatId)
        .map(
            f -> {
              Set<RequestDocument> doc = ImmutableSet.of(document);
              f.setSupportingDocument(doc);
              return floatsRepository.save(f);
            })
        .orElse(null);
  }

  public Floats findById(int floatId) {
    return floatsRepository.findById(floatId).orElse(null);
  }
}
