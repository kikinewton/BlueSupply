package com.logistics.supply.service;

import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.exception.FloatOrderNotFoundException;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.specification.FloatSpecification;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatService {

  private final FloatsRepository floatsRepository;

  public Page<Floats> findAllFloats(int pageNo, int pageSize) {
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
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
    specification.add(new SearchCriteria("fundsReceived", false, SearchOperation.EQUAL));
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
      return floatsRepository.save(floats);
  }

  public Floats findByRef(String floatRef) {
      return floatsRepository.findByFloatRef(floatRef).orElseThrow(() -> new FloatOrderNotFoundException(floatRef));
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

  public Page<Floats> findAll(Pageable pageable) {
    return floatsRepository.findAll(pageable);
  }

  public Floats findById(int floatId) {
    return floatsRepository.findById(floatId).orElseThrow(() -> new FloatOrderNotFoundException(floatId));
  }
}
