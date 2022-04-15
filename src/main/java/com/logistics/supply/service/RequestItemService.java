package com.logistics.supply.service;

import com.logistics.supply.dto.ItemUpdateDTO;
import com.logistics.supply.dto.ReqItems;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.EndorsementStatus.ENDORSED;
import static com.logistics.supply.enums.EndorsementStatus.REJECTED;
import static com.logistics.supply.enums.RequestApproval.APPROVED;
import static com.logistics.supply.enums.RequestStatus.*;

@Service
@Slf4j
@Transactional
public class RequestItemService {

  @Autowired RequestItemRepository requestItemRepository;
  @Autowired SupplierRepository supplierRepository;
  @Autowired EmployeeService employeeService;
  @Autowired CancelledRequestItemRepository cancelledRequestItemRepository;
  @Autowired SupplierRequestMapRepository supplierRequestMapRepository;

  @Value("${config.requestListForSupplier.template}")
  String requestListForSupplier;

  @Autowired private SpringTemplateEngine templateEngine;

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
    try {
      return requestItemRepository.save(item);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  @Cacheable(value = "requestItemsByEmployee", key = "{ #employee}")
  public List<RequestItem> findByEmployee(Employee employee, int pageNo, int pageSize) {
    List<RequestItem> requestItems = new ArrayList<>();
    try {
      Pageable pageable =
          PageRequest.of(
              pageNo, pageSize, Sort.by("id").descending().and(Sort.by("priorityLevel")));
      requestItems.addAll(requestItemRepository.findByEmployee(employee, pageable).getContent());
      return requestItems;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return requestItems;
  }

  public List<RequestItem> getCountNofEmployeeRequest(int count, int employeeId) {
    return requestItemRepository.getEmployeeRequest(employeeId).stream()
        .limit(count)
        .collect(Collectors.toList());
  }

  public Optional<RequestItem> findById(int requestItemId) {
    Optional<RequestItem> requestItem = null;
    try {
      requestItem = requestItemRepository.findById(requestItemId);
      return requestItem;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public boolean supplierIsPresent(RequestItem requestItem, Supplier supplier) {

    requestItem = requestItemRepository.findById(requestItem.getId()).get();

    Set<Supplier> suppliers =
        requestItem.getSuppliers().stream()
            .map(s -> supplierRepository.findById(s.getId()).get())
            .collect(Collectors.toSet());

    return suppliers.stream().anyMatch(s -> s.getId() == supplier.getId());
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem createRequestItem(ReqItems itemDTO, Employee employee) {
    RequestItem requestItem = new RequestItem();
    requestItem.setReason(itemDTO.getReason());
    requestItem.setName(itemDTO.getName());
    requestItem.setPurpose(itemDTO.getPurpose());
    requestItem.setQuantity(itemDTO.getQuantity());
    requestItem.setRequestType(itemDTO.getRequestType());
    requestItem.setUserDepartment(itemDTO.getUserDepartment());
    requestItem.setPriorityLevel(itemDTO.getPriorityLevel());
    String ref =
        IdentifierUtil.idHandler(
            "RQI", employee.getDepartment().getName(), String.valueOf(count()));
    requestItem.setRequestItemRef(ref);
    requestItem.setEmployee(employee);
    try {
      RequestItem result = saveRequestItem(requestItem);
      if (Objects.nonNull(result)) return result;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public long count() {
    return requestItemRepository.count() + 1;
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem endorseRequest(int requestItemId) {
    Optional<RequestItem> requestItem = findById(requestItemId);
    if (requestItem.isPresent()) {
      requestItem.get().setEndorsement(ENDORSED);
      requestItem.get().setEndorsementDate(new Date());
      try {
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          return result;
        }
      } catch (Exception e) {
        log.error(e.toString());
      }
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean approveRequest(int requestItemId) {
    Optional<RequestItem> requestItem = findById(requestItemId);
    try {
      requestItem
          .filter(r -> r.getEndorsement().equals(ENDORSED))
          .filter(r -> r.getStatus().equals(PROCESSED))
          .map(
              r -> {
                r.setApproval(APPROVED);
                r.setApprovalDate(new Date());
                return requestItemRepository.save(r);
              })
          .filter(r -> r.getApproval().equals(APPROVED))
          .isPresent();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return false;
  }

  @Transactional(rollbackFor = Exception.class)
  public CancelledRequestItem cancelRequest(int requestItemId, int employeeId) {
    Employee employee = employeeService.findEmployeeById(employeeId);

    if (employee != null) {

      Optional<RequestItem> requestItem = findById(requestItemId);
      if (requestItem.isPresent() && !requestItem.get().getStatus().equals(APPROVED)) {
        Department department = requestItem.get().getEmployee().getDepartment();
        Employee emp = employeeService.getDepartmentHOD(department);
        requestItem.get().setEndorsement(REJECTED);
        requestItem.get().setEndorsementDate(new Date());
        requestItem.get().setApproval(RequestApproval.REJECTED);
        requestItem.get().setApprovalDate(new Date());
        if (emp.getRoles().get(0).getName().equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())) {
          requestItem.get().setStatus(ENDORSEMENT_CANCELLED);
        } else {

          requestItem.get().setStatus(APPROVAL_CANCELLED);
        }
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          return saveRequest(result, employee, result.getStatus());
        }
      }
    }
    return null;
  }

  public List<RequestItem> findItemsWithFinalSupplier() {
    return requestItemRepository.findBySuppliedByNotNull();
  }

  public List<RequestItem> findItemsWithLpo() {
    return requestItemRepository.findRequestItemsWithLpo();
  }


  public List<RequestItem> getEndorsedItemsWithSuppliers() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getEndorsedRequestItemsWithSuppliersLinked());
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

  public List<RequestItem> getEndorsedItemsWithoutSuppliers() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getEndorsedRequestItemsWithoutSupplier());
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

  public List<RequestItem> getRequestItemsByQuotation(int quotationId) {
    try {
      List<RequestItem> items = new ArrayList<>();
      items.addAll(requestItemRepository.findByQuotationId(quotationId));
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public Optional<RequestItem> findApprovedItemById(int requestItemId) {
    return requestItemRepository.findApprovedRequestById(requestItemId);
  }

  public List<RequestItem> getApprovedItems() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getApprovedRequestItems());
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

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
    return null;
  }

  public List<RequestItem> getRequestItemForHOD(int departmentId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getRequestItemForHOD(departmentId));
      return items;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return items;
  }

  public List<RequestItem> getRequestItemForGeneralManager() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getRequestItemsForGeneralManager());
      return items;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
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
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem assignRequestCategory(int requestItemId, RequestCategory requestCategory) {
    RequestItem requestItem = findById(requestItemId).get();
    requestItem.setRequestCategory(requestCategory);
    return requestItemRepository.save(requestItem);
  }

  @Cacheable(value = "requestItemsBySupplierId", key = "{ #supplierId }")
  public List<RequestItem> findBySupplierId(int supplierId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getRequestItemsBySupplierId(supplierId));
      return items;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return items;
  }

  public List<RequestItem> getEndorsedItemsWithAssignedSuppliers() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getEndorsedRequestItemsWithSuppliersAssigned());
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

  @Cacheable(value = "requestItemsByToBeReviewed", key = "{ #departmentId }")
  public List<RequestItem> findRequestItemsToBeReviewed(
      RequestReview requestReview, int departmentId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(
          requestItemRepository.findByRequestReview(
              requestReview.getRequestReview(), departmentId));
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

  @CacheEvict(value = "requestItemsByToBeReviewed")
  public RequestItem updateRequestReview(int requestItemId, RequestReview requestReview) {
    Optional<RequestItem> requestItem = requestItemRepository.findById(requestItemId);
    if (requestItem.isPresent()) {
      return requestItem
          .map(
              x -> {
                x.setRequestReview(requestReview);
                return requestItemRepository.save(x);
              })
          .orElse(null);
    }

    return null;
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

  @Cacheable(value = "requestItemsByDepartment", key="{#departmentId}")
  public List<RequestItem> getEndorsedRequestItemsForDepartment(int departmentId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getDepartmentEndorsedRequestItemForHOD(departmentId));
      return items;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

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
    context.setVariable(
        "issuedBy", requestItems.stream().findAny().get().getEmployee().getFullName());
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
    System.out.println("file to generate = " + file.getName());
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
        .orElse(null);
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

  public List<RequestItem> getItemsWithFinalPriceUnderQuotation(int quotationId) {
    try {
      return requestItemRepository.findRequestItemsWithFinalPriceByQuotationId(quotationId);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public Set<RequestItem> findRequestItemsWithNoDocumentAttachedForSupplier(int supplierId) {
    return requestItemRepository.findRequestItemsWithNoDocumentAttachedForSupplier(supplierId);
  }

  public boolean priceNotAssigned(List<Integer> requestItemIds) {
    Predicate<RequestItem> hasUnitPrice = r -> r.getSuppliedBy() == null;
    return requestItemRepository.findAllById(requestItemIds).stream().anyMatch(hasUnitPrice);
  }

  public List<RequestItem> findItemsUnderQuotation(int quotationId) {
    return requestItemRepository.findRequestItemsUnderQuotation(quotationId);
  }

  public RequestItem cancelRequestItem(int requestItemId, EmployeeRole employeeRole) {
    return requestItemRepository
        .findById(requestItemId)
        .map(
            r -> {
              if (employeeRole.equals(EmployeeRole.ROLE_GENERAL_MANAGER)) {
                r.setStatus(APPROVAL_CANCELLED);
                return requestItemRepository.save(r);
              } else if (employeeRole.equals(EmployeeRole.ROLE_HOD)) {
                r.setStatus(ENDORSEMENT_CANCELLED);
                return requestItemRepository.save(r);
              }
              return null;
            })
        .orElse(null);
  }

  @Cacheable(value = "requestItemsHistoryByDepartment", key="{#department, #pageNo, #pageSize}")
  public Page<RequestItem> requestItemsHistoryByDepartment(
      Department department, int pageNo, int pageSize) {
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
    return null;
  }
}
