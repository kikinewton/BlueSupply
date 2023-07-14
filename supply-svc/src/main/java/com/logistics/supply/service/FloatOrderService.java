package com.logistics.supply.service;

import com.logistics.supply.dto.FloatDto;
import com.logistics.supply.dto.FloatOrPettyCashDto;
import com.logistics.supply.dto.ItemDto;
import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.event.FloatEvent;
import com.logistics.supply.event.listener.FloatRetirementListener;
import com.logistics.supply.event.listener.FundsReceivedFloatListener;
import com.logistics.supply.exception.FloatOrderNotFoundException;
import com.logistics.supply.exception.RetireFloatOrderException;
import com.logistics.supply.factory.FloatOrderFactory;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.FloatOrderRepository;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.specification.FloatOrderSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import com.logistics.supply.util.FloatOrderValidatorUtil;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
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


  @Cacheable(value = "employeeFloatOrders", key = "{#pageNo,#pageSize, #employee.getId()}")
  public Page<FloatOrder> getEmployeeFloatOrders(int pageNo, int pageSize, Employee employee) {

    log.info("Fetch float orders for employee: {}", employee.getEmail());
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return floatOrderRepository.findByCreatedBy(employee, pageable);
  }

  @Cacheable(value = "floatOrdersPendingGRN", key = "departmentId")
  public Page<FloatOrder> findPendingGrnApprovalFromStoreManager(Pageable pageable, int departmentId){

    log.info("Fetch float orders pending GRN approval from Stores manager");
    return floatOrderRepository.findFloatOrderPendingGrnApprovalFromStoreManager(pageable, departmentId);
  }

  @Cacheable(value = "floatOrdersForEmployee", key = "{#employeeId}")
  public Page<FloatOrder> findByEmployee(int employeeId, Pageable pageable) {

    log.info("Fetch float order for employee with id {}", employeeId);
    return floatOrderRepository.findByCreatedByIdOrderByIdDesc(employeeId, pageable);
  }


  @CacheEvict(value = {"floatOrdersForEmployee", "floatOrdersPendingGRN", "employeeFloatOrders"},
          allEntries = true)
  public FloatOrder addBulkFloatsToFloatOrder(int floatOrderId, Set<FloatDto> items) {

    log.info("Add float items to float order with id: {}", floatOrderId );
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            floatOrder -> {
              Set<Floats> floatItemList = FloatOrderFactory.mapBulkFloatsFromFloatOrder(items, floatOrder);
              floatItemList.forEach(floatOrder::addFloat);
              return floatOrderRepository.save(floatOrder);
            })
        .orElseThrow(() -> new FloatOrderNotFoundException(floatOrderId));
  }



  public Page<FloatOrder> findFloatOrderToClose(int pageNo, int pageSize) {

    log.info("Fetch float orders to be closed");
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(RETIRED, Boolean.FALSE, SearchOperation.EQUAL));

    specification.add(
        new SearchCriteria(GM_RETIREMENT_APPROVAL, Boolean.TRUE, SearchOperation.EQUAL));

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
  }

  public Page<FloatOrder> findFloatOrderByEndorseStatus(
      int pageNo, int pageSize, EndorsementStatus endorsementStatus)  {

    log.info("Fetch float orders by endorse status: {}", endorsementStatus);
    FloatOrderSpecification specification = new FloatOrderSpecification();

    specification.add(new SearchCriteria(ENDORSEMENT, endorsementStatus, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(APPROVAL, RequestApproval.PENDING, SearchOperation.EQUAL));

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
  }

  public Page<FloatOrder> findFloatOrderByRequestStatus(int pageNo, int pageSize, RequestStatus status) {

    log.info("Fetch float orders by request status: {}", status);
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(STATUS, status, SearchOperation.EQUAL));

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
  }



  public Page<FloatOrder> findFloatsAwaitingFunds(int pageNo, int pageSize) {

    log.info("Fetch float orders awaiting funds");
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(
        new SearchCriteria(APPROVAL, RequestApproval.APPROVED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(STATUS, RequestApproval.PENDING, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(RETIRED, Boolean.FALSE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(FUNDS_RECEIVED, Boolean.FALSE, SearchOperation.EQUAL));

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
  }


  public Page<FloatOrder> getFloatOrderWithReceivedFundsAndNotRetired(int pageNo, int pageSize) {

    log.info("Fetch float orders with received funds and not retired");
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(STATUS, RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(FUNDS_RECEIVED, Boolean.TRUE, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(RETIRED, Boolean.FALSE, SearchOperation.EQUAL));

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
  }

  public long count() {
    return floatOrderRepository.countAll() + 1;
  }

  public FloatOrder setFundsAllocatedOnFloatOrder(int floatOrderId, Employee employee) {

    log.info("Set funds allocated on float order id: {}", floatOrderId);
    FloatOrder floatOrder = findById(floatOrderId);
    floatOrder.setFundsReceived(true);
    floatOrder.setStatus(RequestStatus.PROCESSED);
    FloatOrder savedFloatOrder = floatOrderRepository.save(floatOrder);
    sendFloatOrderFundsAllocatedEvent(savedFloatOrder, employee);
    return savedFloatOrder;
  }

  private void sendFloatOrderFundsAllocatedEvent(FloatOrder floatOrder, Employee employee) {

    log.info("Send event for float order funds allocated");
    CompletableFuture.runAsync(
            () -> {
              FundsReceivedFloatListener.FundsReceivedFloatEvent fundsReceivedFloatEvent =
                      new FundsReceivedFloatListener.FundsReceivedFloatEvent(
                              this, employee, floatOrder);
              applicationEventPublisher.publishEvent(fundsReceivedFloatEvent);
            });
  }

  public FloatOrder closeRetirement(int floatOrderId) {

    log.info("Close retirement of float order id: {}", floatOrderId);
    FloatOrder floatOrder = findById(floatOrderId);
    FloatOrderValidatorUtil.isRetirementApprovedByGeneralManager(floatOrder);
    floatOrder.setRetired(true);
    floatOrder.setRetirementDate(new Date());
    return floatOrderRepository.save(floatOrder);

  }

  public Page<FloatOrder> floatOrderForAuditorRetirementApproval(int pageNo, int pageSize) {

    log.info("Fetch float orders ready for retirement approval by auditor");
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(HAS_DOCUMENT, true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(STATUS, RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(
        new SearchCriteria(AUDITOR_RETIREMENT_APPROVAL, null, SearchOperation.IS_NULL));
    specification.add(new SearchCriteria(GM_RETIREMENT_APPROVAL, null, SearchOperation.IS_NULL));

    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
  }

  public Page<FloatOrder> floatOrdersForGmRetirementApproval(int pageNo, int pageSize) {

    log.info("Fetch float orders ready for retirement approval by General manager");
    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(HAS_DOCUMENT, true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(RETIRED, false, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(STATUS, RequestStatus.PROCESSED, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(AUDITOR_RETIREMENT_APPROVAL, true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(GM_RETIREMENT_APPROVAL, null, SearchOperation.IS_NULL));

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);

  }

  public FloatOrder endorse(int floatOrderId, EndorsementStatus status, int endorsedBy) {

    log.info("Endorse float order id: {} by employee id: {}", floatOrderId, endorsedBy);
    FloatOrder floatOrder = findById(floatOrderId);
    floatOrder.setEndorsement(status);
    floatOrder.setEndorsedBy(endorsedBy);
    floatOrder.setEndorsementDate(new Date());

    FloatOrder endorsedFloatOrder = floatOrderRepository.save(floatOrder);
    sendFloatOrderSavedEvent(endorsedFloatOrder);
    return endorsedFloatOrder;
  }

  public FloatOrder approve(int floatOrderId, RequestApproval approval, int approvedBy) {

    FloatOrder floatOrder = findById(floatOrderId);
    floatOrder.setApproval(approval);
    floatOrder.setApprovalDate(new Date());
    floatOrder.setApprovedBy(approvedBy);
    return floatOrderRepository.save(floatOrder);
  }

  @PreAuthorize("hasRole('ROLE_AUDITOR')")
  public FloatOrder approveRetirementByAuditor(int floatOrderId) {

    log.info("Approve retirement of float order with id {} by Auditor", floatOrderId);
    FloatOrder floatOrder = findById(floatOrderId);
      floatOrder.setAuditorRetirementApproval(true);
      floatOrder.setAuditorRetirementApprovalDate(new Date());
    FloatOrder savedFloatOrder = floatOrderRepository.save(floatOrder);
    sendEventForRetirementApprovalForAuditor(savedFloatOrder);
    return savedFloatOrder;
  }

  private void sendEventForRetirementApprovalForAuditor(FloatOrder floatOrder) {

    log.info("Send event after auditor retirement approval");
    CompletableFuture.runAsync(
            () -> {
              FloatRetirementListener.FloatRetirementEvent event =
                      new FloatRetirementListener.FloatRetirementEvent(this, floatOrder);
              applicationEventPublisher.publishEvent(event);
            });
  }

  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER')")
  public FloatOrder approveRetirementByGeneralManager(int floatOrderId) {

    log.info("Approve retirement of float order with id {} by General manager", floatOrderId);
    FloatOrder floatOrder = findById(floatOrderId);
    if (Boolean.TRUE.equals(floatOrder.getAuditorRetirementApproval())) {
      floatOrder.setGmRetirementApproval(true);
      floatOrder.setGmRetirementApprovalDate(new Date());
      FloatOrder approvedFloatOrder = floatOrderRepository.save(floatOrder);
      sendEventForGeneralManagerFloatOrderRetirement(approvedFloatOrder);
      return approvedFloatOrder;
    } else {
      String message = "Float order with id: [%s] retirement not approved by auditor".formatted(floatOrderId);
      throw new RetireFloatOrderException(message);
    }
  }

  private void sendEventForGeneralManagerFloatOrderRetirement(FloatOrder floatOrder) {

    log.info("Send event for General manager retirement approval");
    CompletableFuture.runAsync(() -> applicationEventPublisher.publishEvent(floatOrder));
  }


  public FloatOrder cancel(int floatOrderId, EmployeeRole role) {

    log.info("Cancel float order with id: {} by {}", floatOrderId, role);
    FloatOrder floatOrder = findById(floatOrderId);
    if (role == EmployeeRole.ROLE_HOD) {
      floatOrder.setEndorsement(EndorsementStatus.REJECTED);
      floatOrder.setStatus(RequestStatus.ENDORSEMENT_CANCELLED);
    } else {
      floatOrder.setApproval(RequestApproval.REJECTED);
      floatOrder.setStatus(RequestStatus.APPROVAL_CANCELLED);
    }
    return floatOrderRepository.save(floatOrder);
  }



  /** this service flags float orders that are 2 or more weeks old without being retired */
  @Async
  @Scheduled(fixedDelay = 21600000, initialDelay = 1000)
  public void flagFloatAfter2Weeks() {

    for (FloatOrder floatOrder : floatOrderRepository.findUnRetiredFloats()) {
      boolean isDueForRetirement = FloatOrderValidatorUtil
              .validateDueNonRetiredFloatOrder(floatOrder, DAYS_TO_FLOAT_EXPIRY);

      if (isDueForRetirement) {
        floatOrderRepository.flagFloatOrderAsRetired(floatOrder.getId());
      }
    }
  }


  public FloatOrder uploadSupportingDoc(int floatOrderId, Set<RequestDocument> documents) {

    log.info("Upload documents to float order with id: {}", floatOrderId);
    FloatOrder floatOrder = findById(floatOrderId);
    floatOrder.setHasDocument(true);
    if (floatOrder.getSupportingDocument().isEmpty()) {
      floatOrder.setSupportingDocument(documents);
    } else {
      Set<RequestDocument> prevDoc = floatOrder.getSupportingDocument();
      prevDoc.addAll(documents);
      floatOrder.setSupportingDocument(prevDoc);
    }
    return floatOrderRepository.save(floatOrder);
  }


  public FloatOrder findById(int floatOrderId) {

    log.info("Fetch float order with id: {}", floatOrderId);
    return floatOrderRepository
        .findById(floatOrderId)
        .orElseThrow(() -> new FloatOrderNotFoundException(floatOrderId));
  }


  public FloatOrder updateFloat(int floatOrderId, ItemUpdateDto itemUpdateDto)  {

    log.info("Update float order id: {} with values {}", floatOrderId, itemUpdateDto);
    FloatOrder floatOrder = findById(floatOrderId);
    if (itemUpdateDto.getEstimatedPrice() != null) {
      floatOrder.setAmount(itemUpdateDto.getEstimatedPrice());
    }
    if (itemUpdateDto.getDescription() != null) {
      floatOrder.setDescription(itemUpdateDto.getDescription());
    }
    floatOrder.setStatus(RequestStatus.PENDING);
    return floatOrderRepository.save(floatOrder);
  }

  public Page<FloatOrder> findAllFloatOrder(int pageNo, int pageSize) {

    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return floatOrderRepository.findAll(pageable);
  }


  public Page<FloatOrder> findByApprovalStatus(int pageNo, int pageSize, RequestApproval approval) {

    FloatOrderSpecification specification = new FloatOrderSpecification();
    specification.add(new SearchCriteria(APPROVAL, approval, SearchOperation.EQUAL));
    specification.add(new SearchCriteria(FUNDS_RECEIVED, false, SearchOperation.EQUAL));

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
  }

  public Page<FloatOrder> findFloatsAwaitingGRN(Pageable pageable, int departmentId) {

    log.info("Find float order awaiting GRN for department id: {}", departmentId);
    return floatOrderRepository.findFloatOrderPendingGRN(pageable, departmentId);
  }


  public Page<FloatOrder> findPendingByDepartment(Department department, Pageable pageable) {

    log.info("Fetch float orders pending endorsement for department: {}", department.getName());
    FloatOrderSpecification specification = new FloatOrderSpecification();

      specification.add(new SearchCriteria("department", department, SearchOperation.EQUAL));
      specification.add(
          new SearchCriteria(ENDORSEMENT, EndorsementStatus.PENDING, SearchOperation.EQUAL));
      specification.add(
          new SearchCriteria(APPROVAL, RequestApproval.PENDING, SearchOperation.EQUAL));

      return floatOrderRepository.findAll(specification, pageable);
  }


  public Page<FloatOrder> findFloatsAwaitingDocument(int pageNo, int pageSize, int employeeId) {

    log.info("Fetch float orders pending documents for employee id: {}", employeeId);
      FloatOrderSpecification specification = new FloatOrderSpecification();
      specification.add(
          new SearchCriteria(APPROVAL, RequestApproval.APPROVED, SearchOperation.EQUAL));
      specification.add(new SearchCriteria("createdBy", employeeId, SearchOperation.EQUAL));
      specification.add(new SearchCriteria(FUNDS_RECEIVED, true, SearchOperation.EQUAL));
      specification.add(new SearchCriteria(HAS_DOCUMENT, false, SearchOperation.EQUAL));
      specification.add(new SearchCriteria(RETIRED, false, SearchOperation.EQUAL));

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      return floatOrderRepository.findAll(specification, pageable);
  }

  public FloatOrder saveFloatOrder(
          FloatOrPettyCashDto bulkFloat,
          Employee employee) {

    FloatOrder floatOrder = new FloatOrder();
    floatOrder.setRequestedBy(bulkFloat.getRequestedBy());
    floatOrder.setRequestedByPhoneNo(bulkFloat.getRequestedByPhoneNo());
    floatOrder.setAmount(bulkFloat.getAmount());
    floatOrder.setDepartment(employee.getDepartment());
    floatOrder.setCreatedBy(employee);
    floatOrder.setStaffId(bulkFloat.getStaffId());
    floatOrder.setDescription(bulkFloat.getDescription());
    String ref =
        IdentifierUtil.idHandler(
            "FLT",
            employee.getDepartment().getName(),
            String.valueOf(floatOrderRepository.count()+1));
    floatOrder.setFloatOrderRef(ref);
    bulkFloat.getItems()
        .forEach(
            i -> saveFloatToFloatOrder(employee, floatOrder, ref, i));
    FloatOrder saved = floatOrderRepository.save(floatOrder);
    sendFloatOrderSavedEvent(saved);
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

  private void sendFloatOrderSavedEvent(FloatOrder saved) {
    CompletableFuture.runAsync(
        () -> {
          FloatEvent floatEvent = new FloatEvent(this, saved);
          applicationEventPublisher.publishEvent(floatEvent);
        });
  }

  public List<FloatOrder> findFloatOrdersRequiringGRN(Department department) {

    log.info("Fetch float orders requiring GRN for department: {}", department.getName());
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
