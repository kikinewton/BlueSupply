package com.logistics.supply.service;

import com.logistics.supply.dto.FloatDto;
import com.logistics.supply.dto.FloatOrPettyCashDto;
import com.logistics.supply.dto.ItemDto;
import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.FloatEvent;
import com.logistics.supply.event.listener.FloatRetirementListener;
import com.logistics.supply.exception.FloatOrderNotFoundException;
import com.logistics.supply.exception.RetireFloatOrderException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.FloatOrderRepository;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.specification.FloatOrderSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.FloatOrderValidatorUtil;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FloatOrderService {

  public final FloatsRepository floatsRepository;
  private final FloatOrderRepository floatOrderRepository;
  private final RequestDocumentService requestDocumentService;
  private final ApplicationEventPublisher applicationEventPublisher;

  private static final String APPROVAL = "approval";
  private static final String FUNDS_RECEIVED = "fundsReceived";
  private static final String STATUS = "status";
  public static final String RETIRED = "retired";
  public static final String ENDORSEMENT = "endorsement";
  public static final String HAS_DOCUMENT = "hasDocument";
  public static final String AUDITOR_RETIREMENT_APPROVAL = "auditorRetirementApproval";
  public static final String GM_RETIREMENT_APPROVAL = "gmRetirementApproval";
  private static final int DAYS_TO_FLOAT_EXPIRY = 14;

  public Page<FloatOrder> getAllFloatOrders(int pageNo, int pageSize, boolean retiredStatus) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return floatOrderRepository.findByRetired(retiredStatus, pageable);
  }

  public Page<FloatOrder> getAllEmployeeFloatOrder(int pageNo, int pageSize, Employee employee) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return floatOrderRepository.findByCreatedBy(employee, pageable);
  }

  public Page<FloatOrder> findPendingGrnApprovalFromStoreManager(Pageable pageable, int departmentId){
    return floatOrderRepository.findFloatOrderPendingGrnApprovalFromStoreManager(pageable, departmentId);
  }

  public Page<FloatOrder> findByEmployee(int employeeId, Pageable pageable) {
    return floatOrderRepository.findByCreatedByIdOrderByIdDesc(employeeId, pageable);
  }


  public FloatOrder findByRef(String floatOrderRef) {
    return floatOrderRepository
        .findByFloatOrderRef(floatOrderRef)
        .orElseThrow(() -> new FloatOrderNotFoundException(floatOrderRef));
  }


  public FloatOrder addFloatsToOrder(int floatOrderId, Set<FloatDto> items) {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            o -> {
              Set<Floats> floatItemList = addFloat(items, o);
              floatItemList.forEach(o::addFloat);
              return floatOrderRepository.save(o);
            })
        .orElseThrow(() -> new FloatOrderNotFoundException(floatOrderId));
  }

  private Set<Floats> addFloat(Set<FloatDto> items, FloatOrder o) {
    return items.stream()
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
  }

  public Page<FloatOrder> findFloatOrderAwaitingFunds(int pageNo, int pageSize) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();

    specification.add(
        new SearchCriteria(APPROVAL, RequestApproval.APPROVED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(STATUS, RequestApproval.PENDING, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(RETIRED, Boolean.FALSE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(FUNDS_RECEIVED, Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(Constants.FETCH_FLOAT_FAILED, HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrder> findFloatOrderToClose(int pageNo, int pageSize) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(RETIRED, Boolean.FALSE, SearchOperation.EQUAL));

    specification.add(
        new SearchCriteria(GM_RETIREMENT_APPROVAL, Boolean.TRUE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.FETCH_FLOAT_FAILED, HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrder> findFloatsByEndorseStatus(
      int pageNo, int pageSize, EndorsementStatus endorsementStatus) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();

    specification.add(new SearchCriteria(ENDORSEMENT, endorsementStatus, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(APPROVAL, RequestApproval.PENDING, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(Constants.FETCH_FLOAT_FAILED, HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrder> findFloatsByRequestStatus(int pageNo, int pageSize, RequestStatus status)
      throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(STATUS, status, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(Constants.FETCH_FLOAT_FAILED, HttpStatus.NOT_FOUND);
  }



  public Page<FloatOrder> findFloatsAwaitingFunds(int pageNo, int pageSize) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(
        new SearchCriteria(APPROVAL, RequestApproval.APPROVED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(STATUS, RequestApproval.PENDING, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(RETIRED, Boolean.FALSE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(FUNDS_RECEIVED, Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(Constants.FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }


  public Page<FloatOrder> floatsReceivedFundsAndNotRetired(int pageNo, int pageSize) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(STATUS, RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(FUNDS_RECEIVED, Boolean.TRUE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(RETIRED, Boolean.FALSE, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException(Constants.FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public long count() {
    return floatOrderRepository.countAll() + 1;
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
        .orElseThrow(() -> new FloatOrderNotFoundException(floatOrderId));
  }

  public FloatOrder closeRetirement(int floatOrderId) throws GeneralException {
    return floatOrderRepository
        .findById(floatOrderId)
        .filter(FloatOrder::getGmRetirementApproval)
        .map(
            o -> {
              o.setRetired(true);
              o.setRetirementDate(new Date());
              return floatOrderRepository.save(o);
            })
        .orElseThrow(() -> new GeneralException(Constants.FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public Page<FloatOrder> floatOrderForAuditorRetire(int pageNo, int pageSize)
      throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(HAS_DOCUMENT, true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(STATUS, RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(
        new SearchCriteria(AUDITOR_RETIREMENT_APPROVAL, null, SearchOperation.IS_NULL));
    specification.add(new SearchCriteria(GM_RETIREMENT_APPROVAL, null, SearchOperation.IS_NULL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public Page<FloatOrder> floatOrdersForGmRetire(int pageNo, int pageSize) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(HAS_DOCUMENT, true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(RETIRED, false, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(STATUS, RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(AUDITOR_RETIREMENT_APPROVAL, true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(GM_RETIREMENT_APPROVAL, null, SearchOperation.IS_NULL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public FloatOrder endorse(int floatOrderId, EndorsementStatus status, int endorsedBy) {

    log.info("Endorse float order id: {} by employee id: {}", floatOrderId, endorsedBy);
    FloatOrder floatOrder =
        floatOrderRepository
            .findById(floatOrderId)
            .orElseThrow(() -> new FloatOrderNotFoundException(floatOrderId));
    floatOrder.setEndorsement(status);
    floatOrder.setEndorsedBy(endorsedBy);
    floatOrder.setEndorsementDate(new Date());

    FloatOrder endorsedFloatOrder = floatOrderRepository.save(floatOrder);
    sendFloatSavedEvent(endorsedFloatOrder);
    return endorsedFloatOrder;
  }

  public FloatOrder approve(int floatId, RequestApproval approval, int approvedBy) throws GeneralException {
    FloatOrder floatOrder =
        floatOrderRepository
            .findById(floatId)
            .orElseThrow(() -> new GeneralException(Constants.FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
    floatOrder.setApproval(approval);
    floatOrder.setApprovalDate(new Date());
    floatOrder.setApprovedBy(approvedBy);
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


  public FloatOrder cancel(int floatOrderId, EmployeeRole role) throws GeneralException {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            order -> {
              if (role == EmployeeRole.ROLE_HOD) {
                order.setEndorsement(EndorsementStatus.REJECTED);
                order.setStatus(RequestStatus.ENDORSEMENT_CANCELLED);
              } else {
                order.setApproval(RequestApproval.REJECTED);
                order.setStatus(RequestStatus.APPROVAL_CANCELLED);
              }
              return floatOrderRepository.save(order);
            })
        .orElseThrow(() -> new GeneralException(Constants.FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }


  public FloatOrder retirementApproval(int floatId, EmployeeRole employeeRole) throws GeneralException {
    FloatOrder floatOrder =
        floatOrderRepository
            .findById(floatId)
            .orElseThrow(() -> new FloatOrderNotFoundException(floatId));
    if (!floatOrder.isFundsReceived() && !floatOrder.isRetired())
      throw new GeneralException("FLOAT RETIREMENT FAILED", HttpStatus.FORBIDDEN);
    switch (employeeRole) {
      case ROLE_GENERAL_MANAGER -> {
        floatOrder.setGmRetirementApproval(true);
        floatOrder.setGmRetirementApprovalDate(new Date());
        floatOrder.setRetired(true);
        return floatOrderRepository.save(floatOrder);
      }
      case ROLE_AUDITOR -> {
        floatOrder.setAuditorRetirementApproval(true);
        floatOrder.setAuditorRetirementApprovalDate(new Date());
        return floatOrderRepository.save(floatOrder);
      }
    }
    throw new GeneralException("FLOAT RETIREMENT FAILED", HttpStatus.FORBIDDEN);
  }

  /** this service flags float orders that are 2 or more weeks old without being retired */
  @Async
  @Scheduled(fixedDelay = 21600000, initialDelay = 1000)
  public void flagFloatAfter2Weeks() {
    floatOrderRepository.findUnRetiredFloats()
        .forEach(
            f -> {
              if (FloatOrderValidatorUtil.validateDueNonRetiredFloatOrder(f, DAYS_TO_FLOAT_EXPIRY)) {
                floatOrderRepository.flagFloatOrderAsRetired(f.getId());
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
                prevDoc.addAll(documents);
                f.setSupportingDocument(prevDoc);
              }
              return floatOrderRepository.save(f);
            })
        .orElseThrow(() -> new FloatOrderNotFoundException(floatOrderId));
  }


  public FloatOrder findById(int floatOrderId) {
    return floatOrderRepository
        .findById(floatOrderId)
        .orElseThrow(() -> new FloatOrderNotFoundException(floatOrderId));
  }


  public FloatOrder updateFloat(int floatOrderId, ItemUpdateDto updateDTO)  {
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
        .orElseThrow(() -> new FloatOrderNotFoundException(floatOrderId));
  }

  public Page<FloatOrder> findAllFloatOrder(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return floatOrderRepository.findAll(pageable);
  }


  public Page<FloatOrder> findByApprovalStatus(int pageNo, int pageSize, RequestApproval approval) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(APPROVAL, approval, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(FUNDS_RECEIVED, false, SearchOperation.EQUAL));
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }
  public Page<FloatOrder> findFloatsAwaitingGRN(Pageable pageable, int departmentId) {
    return floatOrderRepository.findFloatOrderPendingGRN(pageable, departmentId);
  }


  public Page<FloatOrder> findPendingByDepartment(Department department, Pageable pageable) throws GeneralException {
    FloatOrderSpecification specification = new FloatOrderSpecification();
    try {
      specification.add(new SearchCriteria("department", department, SearchOperation.EQUAL));
      specification.add(
          new SearchCriteria(ENDORSEMENT, EndorsementStatus.PENDING, SearchOperation.EQUAL));
      specification.add(
          new SearchCriteria(APPROVAL, RequestApproval.PENDING, SearchOperation.EQUAL));
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.FETCH_FLOAT_FAILED, HttpStatus.BAD_REQUEST);
  }


  public Page<FloatOrder> findFloatsAwaitingDocument(int pageNo, int pageSize, int employeeId) throws GeneralException {
    try {
      FloatOrderSpecification specification = new FloatOrderSpecification();
      specification.add(
          new SearchCriteria(APPROVAL, RequestApproval.APPROVED, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("createdBy", employeeId, SearchOperation.EQUAL));
      specification.add(new SearchCriteria(FUNDS_RECEIVED, true, SearchOperation.EQUAL));
      specification.add(new SearchCriteria(HAS_DOCUMENT, false, SearchOperation.EQUAL));
      specification.add(new SearchCriteria(RETIRED, false, SearchOperation.EQUAL));
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND);
  }

  public FloatOrder saveFloatOrder(
          FloatOrPettyCashDto bulkItems,
          Employee employee) {

    FloatOrder order = new FloatOrder();
    order.setRequestedBy(bulkItems.getRequestedBy());
    order.setRequestedByPhoneNo(bulkItems.getRequestedByPhoneNo());
    order.setAmount(bulkItems.getAmount());
    order.setDepartment(employee.getDepartment());
    order.setCreatedBy(employee);
    order.setStaffId(bulkItems.getStaffId());
    order.setDescription(bulkItems.getDescription());
    String ref =
        IdentifierUtil.idHandler(
            "FLT",
            employee.getDepartment().getName(),
            String.valueOf(floatOrderRepository.count()+1));
    order.setFloatOrderRef(ref);
    bulkItems.getItems()
        .forEach(
            i -> saveFloatToFloatOrder(employee, order, ref, i));
    FloatOrder saved = floatOrderRepository.save(order);
    sendFloatSavedEvent(saved);
    return saved;
  }

  private void saveFloatToFloatOrder(
          Employee employee,
          FloatOrder floatOrder,
          String floatRef,
          ItemDto itemDto) {

    Floats fl = new Floats();
    fl.setDepartment(employee.getDepartment());
    fl.setEstimatedUnitPrice(itemDto.getUnitPrice());
    fl.setItemDescription(itemDto.getName());
    fl.setQuantity(itemDto.getQuantity());
    fl.setFloatOrder(floatOrder);
    fl.setCreatedBy(employee);
    fl.setFloatRef(floatRef);
    floatOrder.addFloat(fl);
  }

  private void sendFloatSavedEvent(FloatOrder saved) {
    CompletableFuture.runAsync(
        () -> {
          FloatEvent floatEvent = new FloatEvent(this, saved);
          applicationEventPublisher.publishEvent(floatEvent);
        });
  }

  public List<FloatOrder> findFloatOrdersRequiringGRN(Department department) {
    return floatOrderRepository.findGoodsFloatOrderRequiringGRN(department.getId());
  }

  public FloatOrder retireFloat(int floatOrderId,
                                String email,
                                Set<RequestDocument> documents) {

    log.info("Retire float order with id: {}", floatOrderId);
    FloatOrder floatOrder = findById(floatOrderId);
    boolean loginUserCreatedFloat = floatOrder.getCreatedBy().getEmail().equals(email);

    if (!loginUserCreatedFloat) {
      throw new RetireFloatOrderException("Float order with id %s must be retired by employee who created it"
              .formatted(floatOrderId));
    }

    Set<RequestDocument> requestDocuments =
            documents.stream()
                    .map(l -> requestDocumentService.findById(l.getId()))
                    .collect(Collectors.toSet());

    FloatOrder retiredFloatOrder = uploadSupportingDoc(floatOrderId, requestDocuments);
    sendRetireFloatOrderEvent(retiredFloatOrder);
    return retiredFloatOrder;
  }

  private void sendRetireFloatOrderEvent(FloatOrder floatOrder) {
    CompletableFuture.runAsync(
            () -> {
              FloatRetirementListener.FloatRetirementEvent event =
                      new FloatRetirementListener.FloatRetirementEvent(this, floatOrder);
              applicationEventPublisher.publishEvent(event);
            });
  }
}
