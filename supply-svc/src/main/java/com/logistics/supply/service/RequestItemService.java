package com.logistics.supply.service;

import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.dto.LpoMinorRequestItem;
import com.logistics.supply.dto.RequestItemDto;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.BulkRequestItemEvent;
import com.logistics.supply.exception.FileGenerationException;
import com.logistics.supply.exception.RequestItemNotFoundException;
import com.logistics.supply.factory.RequestItemFactory;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.CancelledRequestItemRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.logistics.supply.repository.SupplierRequestMapRepository;
import com.logistics.supply.specification.RequestItemSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import com.logistics.supply.util.FileGenerationUtil;
import com.logistics.supply.util.RequestItemValidatorUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.EndorsementStatus.ENDORSED;
import static com.logistics.supply.enums.EndorsementStatus.REJECTED;
import static com.logistics.supply.enums.RequestStatus.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RequestItemService {
  private final RequestItemRepository requestItemRepository;
  private final SupplierRepository supplierRepository;
  private final EmployeeService employeeService;

  private final FileGenerationUtil fileGenerationUtil;
  private final CancelledRequestItemRepository cancelledRequestItemRepository;
  private final SupplierRequestMapRepository supplierRequestMapRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Value("${config.requestListForSupplier.template}")
  String requestListForSupplier;

  private final SpringTemplateEngine templateEngine;

  public Page<RequestItem> findAll(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    return requestItemRepository.findAll(pageable);
  }

  @Transactional(readOnly = true)
  public boolean existById(int requestItemId) {
    return requestItemRepository.existsById(requestItemId);
  }

  @Cacheable(value = "requestItemsByEmployee", key = "{ #employee.getId()}")
  public List<RequestItemDto> findByEmployee(Employee employee, int pageNo, int pageSize) {
    List<RequestItemDto> requestItems = new ArrayList<>();
    try {
      Pageable pageable =
          PageRequest.of(
              pageNo, pageSize, Sort.by("id").descending().and(Sort.by("priorityLevel")));
      List<RequestItem> content =
          requestItemRepository.findByEmployee(employee, pageable).getContent();
      content.forEach(
          r -> {
            RequestItemDto requestItemDTO = RequestItemDto.toDto(r);
            requestItems.add(requestItemDTO);
          });
      return requestItems;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return requestItems;
  }

  public RequestItem findById(int requestItemId) {
    return requestItemRepository
        .findById(requestItemId)
        .orElseThrow(() -> new RequestItemNotFoundException(requestItemId));
  }

  public boolean supplierIsPresent(RequestItem requestItem, Supplier supplier) {

    requestItem = findById(requestItem.getId());

    Set<Supplier> suppliers =
        requestItem.getSuppliers().stream()
            .map(s -> supplierRepository.findById(s.getId()).get())
            .collect(Collectors.toSet());

    return suppliers.stream().anyMatch(s -> s.getId().equals(supplier.getId()));
  }

  public List<RequestItemDto> createRequestItem(
      List<LpoMinorRequestItem> items, Employee employee) {

    log.info("Create bulk request items");
    AtomicLong refCount = new AtomicLong(count());

    List<RequestItem> requestItemList =
        items.stream()
            .map(
                lpoMinorRequestItem ->
                    RequestItemFactory.getRequestItem(employee, refCount, lpoMinorRequestItem))
            .collect(Collectors.toList());
    List<RequestItem> requestItems = requestItemRepository.saveAll(requestItemList);
    sendRequestItemsCreatedEvent(requestItems);
    return requestItems.stream().map(RequestItemDto::toDto).collect(Collectors.toList());
  }

  private void sendRequestItemsCreatedEvent(List<RequestItem> requestItems) {
    CompletableFuture.runAsync(
        () -> {
          BulkRequestItemEvent requestItemEvent = null;
          try {
            requestItemEvent = new BulkRequestItemEvent(this, requestItems);
          } catch (Exception e) {
            log.error(e.getMessage());
          }
          applicationEventPublisher.publishEvent(requestItemEvent);
        });
  }

  public long count() {
    return requestItemRepository.countAll() + 1;
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = "requestItemsByDepartment", allEntries = true)
  public RequestItem endorseRequest(int requestItemId) {

    log.info("Endorse request item with id {}", requestItemId);
    RequestItem requestItem = findById(requestItemId);
    RequestItemValidatorUtil.validateRequestItemIsNotEndorsed(requestItem);
    requestItem.setEndorsement(ENDORSED);
    requestItem.setEndorsementDate(new Date());
    return requestItemRepository.save(requestItem);
  }

  public List<RequestItem> endorseBulkRequestItems(List<RequestItem> requestItems) {
    List<RequestItem> endorsedItems =
        requestItems.stream()
            .map(requestItem -> endorseRequest(requestItem.getId()))
            .collect(Collectors.toList());
    sendEndorsedItemsEvent(endorsedItems);
    return endorsedItems;
  }

  private void sendEndorsedItemsEvent(List<RequestItem> endorsedItems) {
    if (endorsedItems.isEmpty()) return;
    CompletableFuture.runAsync(
        () -> {
          BulkRequestItemEvent requestItemEvent = null;
          try {
            requestItemEvent = new BulkRequestItemEvent(this, endorsedItems);
          } catch (Exception e) {
            log.error("Failed to send endorsed items event: {}", e.getMessage());
          }
          assert requestItemEvent != null;
          applicationEventPublisher.publishEvent(requestItemEvent);
        });
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = "requestItemsByDepartment", allEntries = true)
  public boolean approveRequest(int requestItemId) throws GeneralException {

    log.info("Approve request item with id {}", requestItemId);
    RequestItem requestItem = findById(requestItemId);
    RequestItemValidatorUtil.validateRequestItemIsNotApproved(requestItem);
    requestItem.setApproval(RequestApproval.APPROVED);
    requestItem.setApprovalDate(new Date());

    RequestItem item = requestItemRepository.save(requestItem);
    return item.getApproval().equals(RequestApproval.APPROVED);
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = "requestItemsByDepartment", allEntries = true)
  public CancelledRequestItem cancelRequest(int requestItemId, int employeeId) {
    Employee employee = employeeService.findEmployeeById(employeeId);

    RequestItem requestItem = findById(requestItemId);
    Department department = requestItem.getEmployee().getDepartment();
    Employee emp = employeeService.getDepartmentHOD(department);
    requestItem.setEndorsement(REJECTED);
    requestItem.setEndorsementDate(new Date());
    requestItem.setApproval(RequestApproval.REJECTED);
    requestItem.setApprovalDate(new Date());
    requestItem.setDeleted(true);
    if (emp.getRoles().stream()
        .anyMatch(e -> EmployeeRole.ROLE_HOD.name().equalsIgnoreCase(e.getName()))) {
      requestItem.setStatus(ENDORSEMENT_CANCELLED);
    } else {
      requestItem.setStatus(APPROVAL_CANCELLED);
    }
    RequestItem result = requestItemRepository.save(requestItem);
    return saveCancelledRequest(result, employee, result.getStatus());
  }

  public List<RequestItem> getEndorsedItemsWithSuppliers() {
    return requestItemRepository.getEndorsedRequestItemsWithSuppliersLinked();
  }

  public List<RequestItem> getEndorsedItemsWithoutSuppliers() {
    return requestItemRepository.getEndorsedRequestItemsWithoutSupplier();
  }

  public Optional<RequestItem> findApprovedItemById(int requestItemId) {
    return requestItemRepository.findApprovedRequestById(requestItemId);
  }

  public List<RequestItem> getApprovedItems() {
    return requestItemRepository.getApprovedRequestItems();
  }

  private CancelledRequestItem saveCancelledRequest(
      RequestItem requestItem, Employee employee, RequestStatus status) {
    CancelledRequestItem request = new CancelledRequestItem();
    request.setRequestItem(requestItem);
    request.setStatus(status);
    request.setEmployee(employee);
    return cancelledRequestItemRepository.save(request);
  }

  @Cacheable(value = "requestItemsForHod", key = "#departmentId", cacheManager = "rqCacheManager")
  public List<RequestItemDto> getRequestItemForHOD(int departmentId) {
    List<RequestItem> requestItemForHOD = requestItemRepository.getRequestItemForHOD(departmentId);
    return requestItemForHOD.stream().map(RequestItemDto::toDto).collect(Collectors.toList());
  }

  public List<RequestItemDto> getRequestItemDtoForHOD(int departmentId) {
    List<RequestItem> requestItemForHOD = requestItemRepository.getRequestItemForHOD(departmentId);
    return requestItemForHOD.stream().map(RequestItemDto::toDto).collect(Collectors.toList());
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = "requestItemsByDepartment", allEntries = true)
  public RequestItem assignSuppliersToRequestItem(
      RequestItem requestItem, Set<Supplier> suppliers) {
    requestItem.setSuppliers(suppliers);

    RequestItem result = requestItemRepository.save(requestItem);
    suppliers.forEach(
        s -> {
          SupplierRequestMap map = new SupplierRequestMap(s, requestItem);
          supplierRequestMapRepository.save(map);
        });
    return result;
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem assignRequestCategory(int requestItemId, RequestCategory requestCategory) {
    RequestItem requestItem = findById(requestItemId);
    requestItem.setRequestCategory(requestCategory);
    return requestItemRepository.save(requestItem);
  }

  @Cacheable(value = "requestItemsBySupplierId", key = "#supplierId")
  public List<RequestItem> findBySupplierId(int supplierId) {
    return requestItemRepository.getRequestItemsBySupplierId(supplierId);
  }

  //  @Cacheable(value = "endorsedRequestItemsWithSuppliers")
  public List<RequestItem> getEndorsedItemsWithAssignedSuppliers() {
    return requestItemRepository.getEndorsedRequestItemsWithSuppliersAssigned();
  }

  public List<RequestItem> findRequestItemsToBeReviewed(
      RequestReview requestReview, int departmentId) {
    return requestItemRepository.findByRequestReview(
        requestReview.getRequestReview(), departmentId);
  }

  public List<RequestItemDto> findRequestItemsDtoToBeReviewed(
      RequestReview requestReview, int departmentId) {
    List<RequestItem> requestReview1 =
        requestItemRepository.findByRequestReview(requestReview.getRequestReview(), departmentId);
    return requestReview1.stream().map(RequestItemDto::toDto).collect(Collectors.toList());
  }

  @CacheEvict(value = {"requestItemsByToBeReviewed", "requestItemsByDepartment"})
  public RequestItem updateRequestReview(int requestItemId, RequestReview requestReview) {
    RequestItem requestItem = findById(requestItemId);
    requestItem.setRequestReview(requestReview);
    return requestItemRepository.save(requestItem);
  }

  public List<RequestItem> findRequestItemsWithoutDocInQuotation() {

    log.info("Find request items without quotation assigned");
    List<Integer> requestItemIdsWithoutQuotation =
        requestItemRepository.findItemIdWithoutDocsInQuotation();

    return requestItemIdsWithoutQuotation.stream()
        .map(requestItemId -> findById(requestItemId))
        .collect(Collectors.toList());
  }

  @Cacheable(
      value = "requestItemsByDepartment",
      key = "{#departmentId}",
      unless = "result.isEmpty == true")
  public List<RequestItem> getEndorsedRequestItemsForDepartment(int departmentId) {
    return requestItemRepository.getDepartmentEndorsedRequestItemForHOD(departmentId);
  }

  @Cacheable(
      value = "requestItemsForSupplier",
      key = "#supplierId",
      unless = "#result.isEmpty == true")
  public Set<RequestItem> findRequestItemsForSupplier(int supplierId) {

    List<Integer> idList = new ArrayList<>();
    idList.addAll(requestItemRepository.findUnprocessedRequestItemsForSupplier(supplierId));
    return idList.stream().map(this::findById).collect(Collectors.toSet());
  }

  public File generateRequestListForSupplier(int supplierId) {

    log.info("Generate items file for supplier with id: {}", supplierId);
    Set<RequestItem> requestItems = findRequestItemsForSupplier(supplierId);
    if (requestItems.isEmpty()) {
      throw new FileGenerationException(
          "Supplier with id %s has no LPO request assigned".formatted(supplierId));
    }
    String supplier = supplierRepository.findById(supplierId).get().getName();
    Context context = new Context();

    String pattern = "EEEEE dd MMMMM yyyy";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("en", "UK"));
    String trDate = simpleDateFormat.format(new Date());
    context.setVariable("supplier", supplier);
    context.setVariable("requestItems", requestItems);
    context.setVariable("date", trDate);

    String html = parseThymeleafTemplate(context);
    String pdfName = trDate.replace(" ", "").concat("_list_").concat(supplier.replace(" ", ""));
    return fileGenerationUtil.generatePdfFromHtml(html, pdfName).join();
  }

  private String parseThymeleafTemplate(Context context) {

    return templateEngine.process(requestListForSupplier, context);
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem updateItemQuantity(
      int requestId, ItemUpdateDTO itemUpdateDTO, String employeeEmail) {

    log.info("Update request item with id {} with values {}", requestId, itemUpdateDTO);
    RequestItem requestItem = findById(requestId);
    RequestItemValidatorUtil.validateRequestItemCanBeUpdated(requestItem, employeeEmail);
    if (itemUpdateDTO.getQuantity() != null) requestItem.setQuantity(itemUpdateDTO.getQuantity());
    if (itemUpdateDTO.getDescription() != null) requestItem.setName(itemUpdateDTO.getDescription());
    requestItem.setStatus(PENDING);
    return requestItemRepository.save(requestItem);
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"requestItemsHistoryByDepartment", "requestItemsByDepartment", "requestItemsForHod"},
      allEntries = true)
  public void resolveCommentOnRequest(int requestItemId) {

    log.info("Resolve comment on request item with id: {}", requestItemId);
    RequestItem requestItem = findById(requestItemId);
    requestItem.setStatus(PENDING);
    requestItemRepository.save(requestItem);
  }

  /**
   * This method assigns the unit price, supplier and request category to the request item. The
   * status of the request is also changed to process and request_review set to HOD_REVIEW in this
   * process.
   */
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {
        "itemsWithPriceByQuotationId",
        "requestItemsHistoryByDepartment",
        "itemsByQuotationId",
        "itemsWithNoDocBySupplierId",
        "requestItemsByDepartment",
        "requestItemsForHod",
        "requestItemsForSupplier"
      },
      allEntries = true)
  public Set<RequestItem> assignProcurementDetailsToItems(List<RequestItem> requestItems) {

    log.info("Assign procurement details to list of request items");
    return requestItems.stream()
        .filter(
            r ->
                (Objects.nonNull(r.getUnitPrice())
                    && Objects.nonNull(r.getRequestCategory())
                    && Objects.nonNull(r.getSuppliedBy())))
        .map(this::assignProcurementDetails)
        .collect(Collectors.toSet());
  }

  private RequestItem assignProcurementDetails(RequestItem requestItem) {

    log.info("Assign procurement details to request item with id: {}", requestItem.getId());
    RequestItem item = findById(requestItem.getId());
    item.setSuppliedBy(requestItem.getSuppliedBy());
    item.setUnitPrice(requestItem.getUnitPrice());
    item.setRequestCategory(requestItem.getRequestCategory());
    item.setStatus(RequestStatus.PROCESSED);
    item.setCurrency(requestItem.getCurrency());
    item.setRequestReview(RequestReview.PENDING);
    double totalPrice =
        Double.parseDouble(String.valueOf(requestItem.getUnitPrice())) * requestItem.getQuantity();
    item.setTotalPrice(BigDecimal.valueOf(totalPrice));
    return requestItemRepository.save(item);
  }

  @Cacheable(
      value = "itemsWithPriceByQuotationId",
      key = "#quotationId",
      unless = "#result.isEmpty == true")
  public List<RequestItem> getItemsWithFinalPriceUnderQuotation(int quotationId) {

    log.info("Fetch lpo items final price for quotation id: {}", quotationId);
    return requestItemRepository.findRequestItemsWithFinalPriceByQuotationId(quotationId);
  }

  @Cacheable(
      value = "itemsWithNoDocBySupplierId",
      key = "#supplierId",
      unless = "#result.isEmpty == true")
  public Set<RequestItem> findRequestItemsWithNoDocumentAttachedForSupplier(int supplierId) {

    log.info("Fetch lpo items with no document assigned to supplier with id: {}", supplierId);
    return requestItemRepository.findRequestItemsWithNoDocumentAttachedForSupplier(supplierId);
  }

  public boolean priceNotAssigned(List<Integer> requestItemIds) {
    Predicate<RequestItem> hasUnitPrice = r -> r.getSuppliedBy() == null;
    return requestItemRepository.findAllById(requestItemIds).stream().anyMatch(hasUnitPrice);
  }

  @Cacheable(value = "itemsByQuotationId", key = "#quotationId", unless = "#result.isEmpty == true")
  public List<RequestItem> findItemsUnderQuotation(int quotationId) {

    log.info("Fetch items with quotation id: {}", quotationId);
    return requestItemRepository.findRequestItemsUnderQuotation(quotationId);
  }

  @Cacheable(
      value = "requestItemsHistoryByDepartment",
      key = "{#department.getId(), #pageNo, #pageSize}",
      unless = "#result.isEmpty == true")
  public Page<RequestItem> requestItemsHistoryByDepartment(
      Department department, int pageNo, int pageSize) throws GeneralException {
    RequestItemSpecification specification = new RequestItemSpecification();
    specification.add(
        new SearchCriteria("userDepartment", department.getId(), SearchOperation.EQUAL));
    specification.add(new SearchCriteria("endorsement", ENDORSED, SearchOperation.EQUAL));

    Pageable pageable =
        PageRequest.of(
            pageNo, pageSize, Sort.by("id").descending().and(Sort.by("updatedDate").descending()));
    return requestItemRepository.findAll(specification, pageable);
  }
}
