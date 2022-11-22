package com.logistics.supply.service;

import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.dto.ReqItems;
import com.logistics.supply.dto.RequestItemDTO;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.BulkRequestItemEvent;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.CancelledRequestItemRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.logistics.supply.repository.SupplierRequestMapRepository;
import com.logistics.supply.specification.RequestItemSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import com.logistics.supply.util.IdentifierUtil;
import com.lowagie.text.DocumentException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.EndorsementStatus.ENDORSED;
import static com.logistics.supply.enums.EndorsementStatus.REJECTED;
import static com.logistics.supply.enums.RequestApproval.APPROVED;
import static com.logistics.supply.enums.RequestStatus.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RequestItemService {
  private final RequestItemRepository requestItemRepository;
  private final SupplierRepository supplierRepository;
  private final EmployeeService employeeService;
  private final CancelledRequestItemRepository cancelledRequestItemRepository;
  private final SupplierRequestMapRepository supplierRequestMapRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Value("${config.requestListForSupplier.template}")
  String requestListForSupplier;

  private final SpringTemplateEngine templateEngine;

  public List<RequestItem> findAll(int pageNo, int pageSize) {
    List<RequestItem> requestItemList = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
      Page<RequestItem> items = requestItemRepository.findAll(pageable);
      requestItemList.addAll(items.getContent());
    } catch (Exception e) {
      log.error(e.toString());
    }
    return requestItemList;
  }

  @Transactional(readOnly = true)
  public boolean existById(int requestItemId) {
    return requestItemRepository.existsById(requestItemId);
  }

  public RequestItem saveRequestItem(RequestItem item) {
    return requestItemRepository.save(item);
  }

  @Cacheable(value = "requestItemsByEmployee", key = "{ #employee.getId()}")
  public List<RequestItemDTO> findByEmployee(Employee employee, int pageNo, int pageSize) {
    List<RequestItemDTO> requestItems = new ArrayList<>();
    try {
      Pageable pageable =
          PageRequest.of(
              pageNo, pageSize, Sort.by("id").descending().and(Sort.by("priorityLevel")));
      List<RequestItem> content =
          requestItemRepository.findByEmployee(employee, pageable).getContent();
      content.forEach(
          r -> {
            RequestItemDTO requestItemDTO = RequestItemDTO.toDto(r);
            requestItems.add(requestItemDTO);
          });
      return requestItems;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return requestItems;
  }

  public Optional<RequestItem> findById(int requestItemId) {
    return requestItemRepository.findById(requestItemId);
  }

  public boolean supplierIsPresent(RequestItem requestItem, Supplier supplier) {

    requestItem = requestItemRepository.findById(requestItem.getId()).get();

    Set<Supplier> suppliers =
        requestItem.getSuppliers().stream()
            .map(s -> supplierRepository.findById(s.getId()).get())
            .collect(Collectors.toSet());

    return suppliers.stream().anyMatch(s -> s.getId() == supplier.getId());
  }

  public List<RequestItemDTO> createRequestItem(List<ReqItems> items, Employee employee) {
    AtomicLong refCount = new AtomicLong(count());
    List<RequestItem> rqi =
        items.stream()
            .map(
                r -> {
                  RequestItem requestItem = new RequestItem();
                  requestItem.setReason(r.getReason());
                  requestItem.setName(r.getName());
                  requestItem.setPurpose(r.getPurpose());
                  requestItem.setQuantity(r.getQuantity());
                  requestItem.setRequestType(r.getRequestType());
                  requestItem.setUserDepartment(r.getUserDepartment());
                  requestItem.setPriorityLevel(r.getPriorityLevel());
                  String ref =
                      IdentifierUtil.idHandler(
                          "RQI",
                          employee.getDepartment().getName(),
                          String.valueOf(refCount.get()));
                  requestItem.setRequestItemRef(ref);
                  refCount.incrementAndGet();
                  requestItem.setEmployee(employee);
                  return requestItem;
                })
            .collect(Collectors.toList());
    List<RequestItem> requestItems = requestItemRepository.saveAll(rqi);
    CompletableFuture.runAsync(
        () -> {
          BulkRequestItemEvent requestItemEvent = null;
          try {
            requestItemEvent = new BulkRequestItemEvent(this, requestItems);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          applicationEventPublisher.publishEvent(requestItemEvent);
        });
    return requestItems.stream().map(RequestItemDTO::toDto).collect(Collectors.toList());
  }

  public long count() {
    return requestItemRepository.countAll() + 1;
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = "requestItemsByDepartment", allEntries = true)
  public RequestItem endorseRequest(int requestItemId) {
    Optional<RequestItem> requestItem = findById(requestItemId);
    if (requestItem.isPresent()) {
      requestItem.get().setEndorsement(ENDORSED);
      requestItem.get().setEndorsementDate(new Date());
      try {
        return requestItemRepository.save(requestItem.get());
      } catch (Exception e) {
        log.error(e.toString());
      }
    }
    throw new GeneralException("ENDORSE REQUEST ITEM FAILED", HttpStatus.BAD_REQUEST);
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = "requestItemsByDepartment", allEntries = true)
  public boolean approveRequest(int requestItemId) throws GeneralException {
    RequestItem requestItem =
        findById(requestItemId)
            .orElseThrow(
                () -> new GeneralException("Request item not found", HttpStatus.NOT_FOUND));
    if(!requestItem.getEndorsement().equals(ENDORSED)) throw new GeneralException("Request item not endorsed", HttpStatus.BAD_REQUEST);
    if(!requestItem.getStatus().equals(PROCESSED)) throw new GeneralException("Request item not processed", HttpStatus.BAD_REQUEST);
    requestItem.setApproval(APPROVED);
    requestItem.setApprovalDate(new Date());
    try {
      RequestItem item = requestItemRepository.save(requestItem);
      return item.getApproval().equals(APPROVED);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return false;
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = "requestItemsByDepartment", allEntries = true)
  public CancelledRequestItem cancelRequest(int requestItemId, int employeeId) {
    Employee employee = employeeService.findEmployeeById(employeeId);

    RequestItem requestItem =
        requestItemRepository
            .findById(requestItemId)
            .orElseThrow(
                () -> new GeneralException("REQUEST ITEM NOT FOUND", HttpStatus.NOT_FOUND));
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
    return saveRequest(result, employee, result.getStatus());
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

  @SneakyThrows
  public CancelledRequestItem saveRequest(
      RequestItem requestItem, Employee employee, RequestStatus status) {
    CancelledRequestItem request = new CancelledRequestItem();
    request.setRequestItem(requestItem);
    request.setStatus(status);
    request.setEmployee(employee);
    try {
      return cancelledRequestItemRepository.save(request);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("CANCEL REQUEST ITEM FAILED", HttpStatus.BAD_REQUEST);
  }

  @Cacheable(value = "requestItemsForHod", key = "#departmentId", cacheManager = "rqCacheManager")
  public List<RequestItemDTO> getRequestItemForHOD(int departmentId) {
    List<RequestItem> requestItemForHOD = requestItemRepository.getRequestItemForHOD(departmentId);
    return requestItemForHOD.stream().map(RequestItemDTO::toDto).collect(Collectors.toList());
  }

  public List<RequestItemDTO> getRequestItemDtoForHOD(int departmentId) {
    List<RequestItem> requestItemForHOD = requestItemRepository.getRequestItemForHOD(departmentId);
    return requestItemForHOD.stream().map(RequestItemDTO::toDto).collect(Collectors.toList());
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = "requestItemsByDepartment", allEntries = true)
  public RequestItem assignSuppliersToRequestItem(
      RequestItem requestItem, Set<Supplier> suppliers) {
    requestItem.setSuppliers(suppliers);

    RequestItem result = requestItemRepository.save(requestItem);
    if (Objects.nonNull(result)) {
      suppliers.forEach(
          s -> {
            SupplierRequestMap map = new SupplierRequestMap(s, requestItem);
            supplierRequestMapRepository.save(map);
          });
      return result;
    }
    throw new GeneralException("ASSIGN SUPPLIERS TO REQUEST ITEM FAILED", HttpStatus.BAD_REQUEST);
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem assignRequestCategory(int requestItemId, RequestCategory requestCategory) {
    RequestItem requestItem = findById(requestItemId).get();
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

  public List<RequestItemDTO> findRequestItemsDtoToBeReviewed(
      RequestReview requestReview, int departmentId) {
    List<RequestItem> requestReview1 =
        requestItemRepository.findByRequestReview(requestReview.getRequestReview(), departmentId);
    return requestReview1.stream().map(RequestItemDTO::toDto).collect(Collectors.toList());
  }

  @SneakyThrows
  @CacheEvict(value = {"requestItemsByToBeReviewed", "requestItemsByDepartment"})
  public RequestItem updateRequestReview(int requestItemId, RequestReview requestReview) {
    RequestItem requestItem =
        requestItemRepository
            .findById(requestItemId)
            .orElseThrow(
                () -> new GeneralException("REQUEST ITEM NOT FOUND", HttpStatus.NOT_FOUND));
    requestItem.setRequestReview(requestReview);
    return requestItemRepository.save(requestItem);
  }

  public List<RequestItem> findRequestItemsWithoutDocInQuotation() {
    List<RequestItem> items = new ArrayList<>();
    try {
      List<Integer> ids = requestItemRepository.findItemIdWithoutDocsInQuotation();
      if (ids.size() > 0) {
        items.addAll(
            ids.stream()
                .map(x -> requestItemRepository.findById(x).get())
                .collect(Collectors.toList()));
        return items;
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

  @Cacheable(value = "requestItemsByDepartment", key = "{#departmentId}")
  public List<RequestItem> getEndorsedRequestItemsForDepartment(int departmentId) {
    return requestItemRepository.getDepartmentEndorsedRequestItemForHOD(departmentId);
  }

  @Cacheable(
      value = "requestItemsForSupplier",
      key = "#supplierId",
      unless = "#result.isEmpty == true")
  public Set<RequestItem> findRequestItemsForSupplier(int supplierId) {
    Set<RequestItem> items = new HashSet<>();
    List<Integer> idList = new ArrayList<>();
    try {
      idList.addAll(requestItemRepository.findUnprocessedRequestItemsForSupplier(supplierId));
      items = idList.stream().map(x -> findById(x).get()).collect(Collectors.toSet());
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

  public File generateRequestListForSupplier(int supplierId) throws DocumentException, IOException {
    Set<RequestItem> requestItems = findRequestItemsForSupplier(supplierId);
    if (requestItems.size() < 0) return null;
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
    return generatePdfFromHtml(html, pdfName);
  }

  private String parseThymeleafTemplate(Context context) {

    return templateEngine.process(requestListForSupplier, context);
  }

  private File generatePdfFromHtml(String html, String pdfName)
      throws IOException, DocumentException {
    File file = File.createTempFile(pdfName, ".pdf");

    OutputStream outputStream = new FileOutputStream(file);
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(html);
    renderer.layout();
    renderer.createPDF(outputStream);
    outputStream.close();
    if (Objects.isNull(file)) System.out.println("file is null");

    return file;
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem updateItemQuantity(int requestId, ItemUpdateDTO itemUpdateDTO)
      throws Exception {
    return findById(requestId)
        .map(
            x -> {
              if (itemUpdateDTO.getQuantity() != null) x.setQuantity(itemUpdateDTO.getQuantity());
              if (itemUpdateDTO.getDescription() != null) x.setName(itemUpdateDTO.getDescription());
              x.setStatus(PENDING);
              return requestItemRepository.save(x);
            })
        .orElseThrow(
            () -> new GeneralException("UPDATE REQUEST ITEM FAILED", HttpStatus.BAD_REQUEST));
  }

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"requestItemsHistoryByDepartment", "requestItemsByDepartment", "requestItemsForHod"},
      allEntries = true)
  public void resolveCommentOnRequest(int requestItemId) {
    findById(requestItemId)
        .ifPresent(
            r -> {
              r.setStatus(PENDING);
              requestItemRepository.save(r);
            });
  }

  /**
   * This method assigns the unit price, supplier and request category to the request item. The
   * status of the request is also changed to process and request_review set to HOD_REVIEW in this
   * process.
   *
   * @param requestItems
   * @return
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
    Set<RequestItem> result =
        requestItems.stream()
            .filter(
                r ->
                    (Objects.nonNull(r.getUnitPrice())
                        && Objects.nonNull(r.getRequestCategory())
                        && Objects.nonNull(r.getSuppliedBy())))
            .map(
                i -> {
                  RequestItem item = findById(i.getId()).get();
                  item.setSuppliedBy(i.getSuppliedBy());
                  item.setUnitPrice(i.getUnitPrice());
                  item.setRequestCategory(i.getRequestCategory());
                  item.setStatus(RequestStatus.PROCESSED);
                  item.setCurrency(i.getCurrency());
                  item.setRequestReview(RequestReview.PENDING);
                  double totalPrice =
                      Double.parseDouble(String.valueOf(i.getUnitPrice())) * i.getQuantity();
                  item.setTotalPrice(BigDecimal.valueOf(totalPrice));
                  return requestItemRepository.save(item);
                })
            .collect(Collectors.toSet());
    return result;
  }

  @Cacheable(
      value = "itemsWithPriceByQuotationId",
      key = "#quotationId",
      unless = "#result.isEmpty == true")
  public List<RequestItem> getItemsWithFinalPriceUnderQuotation(int quotationId) {
    return requestItemRepository.findRequestItemsWithFinalPriceByQuotationId(quotationId);
  }

  @Cacheable(
      value = "itemsWithNoDocBySupplierId",
      key = "#supplierId",
      unless = "#result.isEmpty == true")
  public Set<RequestItem> findRequestItemsWithNoDocumentAttachedForSupplier(int supplierId) {
    return requestItemRepository.findRequestItemsWithNoDocumentAttachedForSupplier(supplierId);
  }

  public boolean priceNotAssigned(List<Integer> requestItemIds) {
    Predicate<RequestItem> hasUnitPrice = r -> r.getSuppliedBy() == null;
    return requestItemRepository.findAllById(requestItemIds).stream().anyMatch(hasUnitPrice);
  }

  @Cacheable(value = "itemsByQuotationId", key = "#quotationId", unless = "#result.isEmpty == true")
  public List<RequestItem> findItemsUnderQuotation(int quotationId) {
    return requestItemRepository.findRequestItemsUnderQuotation(quotationId);
  }

  @Cacheable(
      value = "requestItemsHistoryByDepartment",
      key = "{#department.getId(), #pageNo, #pageSize}")
  public Page<RequestItem> requestItemsHistoryByDepartment(
      Department department, int pageNo, int pageSize) throws GeneralException {
    RequestItemSpecification specification = new RequestItemSpecification();
    specification.add(
        new SearchCriteria("userDepartment", department.getId(), SearchOperation.EQUAL));
    specification.add(new SearchCriteria("endorsement", ENDORSED, SearchOperation.EQUAL));
    try {
      Pageable pageable =
          PageRequest.of(
              pageNo,
              pageSize,
              Sort.by("id").descending().and(Sort.by("updatedDate").descending()));
      return requestItemRepository.findAll(specification, pageable);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException("REQUEST ITEMS HISTORY NOT FOUND", HttpStatus.NOT_FOUND);
  }
}
