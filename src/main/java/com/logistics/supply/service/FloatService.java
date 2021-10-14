package com.logistics.supply.service;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.specification.FloatSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("created_date").descending());
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
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
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
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Page<Floats> findFloatsByRequestStatus(int pageNo, int pageSize, RequestStatus status) {
    FloatSpecification specification = new FloatSpecification();
    specification.add(
        new SearchCriteria("status", status.getRequestStatus(), SearchOperation.EQUAL));
    System.out.println("specification = " + specification);
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
      return floatsRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public long count() {
    return floatsRepository.count();
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
    }
    return null;
  }

  public Floats cancel(int floatId) {
    try {
      //      Floats f = floatsRepository.findById(floatId).map(x -> {
      //        x.setStatus(RequestStatus.);
      //      })

    } catch (Exception e) {
      log.error(e.getMessage());
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

  public void flagFloatAfter2Weeks() {
    // todo create a service to flag floats that are 2 or more weeks old
  }
}
