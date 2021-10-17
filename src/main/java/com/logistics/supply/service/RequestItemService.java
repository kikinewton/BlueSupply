package com.logistics.supply.service;

import com.logistics.supply.dto.ReqItems;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.*;
import com.logistics.supply.util.IdentifierUtil;
import com.lowagie.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.text.SimpleDateFormat;
import java.util.*;
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
  @Autowired EmployeeRepository employeeRepository;
  @Autowired CancelledRequestItemRepository cancelledRequestItemRepository;
  @Autowired SupplierRequestMapRepository supplierRequestMapRepository;

  @Value("${config.requestListForSupplier.template}")
  String requestListForSupplier;

  @Autowired private SpringTemplateEngine templateEngine;

  public List<RequestItem> findAll(int pageNo, int pageSize) {
    List<RequestItem> requestItemList = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate").descending());
      Page<RequestItem> items = requestItemRepository.findAll(pageable);
      items.forEach(requestItemList::add);
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

  public List<RequestItem> findByEmployee(Employee employee, int pageNo, int pageSize) {
    List<RequestItem> requestItems = new ArrayList<>();
    try {
      Pageable pageable =
          PageRequest.of(
              pageNo, pageSize, Sort.by("createdDate").descending().and(Sort.by("priorityLevel")));
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
    log.info("requestItem id= " + requestItem.getId().toString());
    Set<Supplier> suppliers =
        requestItem.getSuppliers().stream()
            .map(s -> supplierRepository.findById(s.getId()).get())
            .collect(Collectors.toSet());
    log.info("Supplier is present with size: " + suppliers.size());

    return suppliers.stream().anyMatch(s -> s.getId() == supplier.getId());
  }

  public RequestItem createRequestItem(ReqItems itemDTO, Employee employee) {
    RequestItem requestItem = new RequestItem();
    requestItem.setReason(itemDTO.getReason());
    requestItem.setName(itemDTO.getName());
    requestItem.setPurpose(itemDTO.getPurpose());
    requestItem.setQuantity(itemDTO.getQuantity());
    requestItem.setRequestType(itemDTO.getRequestType());
    requestItem.setUserDepartment(employee.getDepartment());
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
    return requestItemRepository.count();
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
    System.out.println("Cancel process initialised");
    Optional<Employee> employee = employeeRepository.findById(employeeId);

    if (employee.isPresent()) {

      Optional<RequestItem> requestItem = findById(requestItemId);
      if (requestItem.isPresent() && !requestItem.get().getStatus().equals(APPROVED)) {
        System.out.println("Cancel Request is valid");
        int deptId = requestItem.get().getEmployee().getDepartment().getId();
        Employee emp =
            employeeRepository.findDepartmentHod(deptId, EmployeeRole.ROLE_HOD.ordinal());
        requestItem.get().setEndorsement(REJECTED);
        requestItem.get().setEndorsementDate(new Date());
        requestItem.get().setApproval(RequestApproval.REJECTED);
        requestItem.get().setApprovalDate(new Date());
        if (emp.getId().equals(employee.get().getId())) {
          requestItem.get().setStatus(ENDORSEMENT_CANCELLED);
        } else {

          requestItem.get().setStatus(APPROVAL_CANCELLED);
        }
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          return saveRequest(result, employee.get(), result.getStatus());
        }
      }
    }
    return null;
  }

  public List<RequestItem> getEndorsedItems() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getEndorsedRequestItems());
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
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

  public List<RequestItem> findRequestItemsToBeReviewed(RequestReview requestReview) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.findByRequestReview(requestReview.getRequestReview()));
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

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

  public List<RequestItem> getEndorsedFloatOrPettyCash() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.findEndorsedFloatOrPettyCashList());
      return items;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public List<RequestItem> getGMApprovedFloatOrPettyCash() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.findGMApprovedFloatOrPettyCashList());
      return items;
    } catch (Exception e) {
      log.error(e.getMessage());
      log.error(e.toString());
    }
    return null;
  }

  public Set<RequestItem> findRequestItemsForSupplier(int supplierId) {
    Set<RequestItem> items = new HashSet<>();
    List<Integer> idList = new ArrayList<>();
    try {
      idList.addAll(requestItemRepository.findRequestItemsForSupplier(supplierId));
      items = idList.stream().map(x -> findById(x).get()).collect(Collectors.toSet());
      return items;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return items;
  }

  public File generateRequestListForSupplier(int supplierId, Employee employee)
      throws DocumentException, IOException {
    var requestItems = findRequestItemsForSupplier(supplierId);
    if (requestItems.size() < 0) return null;
    String supplier = supplierRepository.findById(supplierId).get().getName();
    Context context = new Context();

    String pattern = "EEEEE dd MMMMM yyyy";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, new Locale("en", "UK"));
    String trDate = simpleDateFormat.format(new Date());
    context.setVariable("supplier", supplier);
    context.setVariable("requestItems", requestItems);
    context.setVariable("date", trDate);
    context.setVariable("issuedBy", employee.getFullName());
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
    System.out.println("step 2");
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(html);
    renderer.layout();
    renderer.createPDF(outputStream);
    outputStream.close();
    if (Objects.isNull(file)) System.out.println("file is null");
    System.out.println("file to generate = " + file.getName());
    return file;
  }

  public RequestItem updateItemQuantity(int requestId, int quantity) throws Exception {
    return findById(requestId)
        .map(
            x -> {
              x.setQuantity(quantity);
              return requestItemRepository.save(x);
            })
        .orElse(null);
  }
}
