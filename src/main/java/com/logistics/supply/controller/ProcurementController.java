package com.logistics.supply.controller;

import com.logistics.supply.auth.AppUserDetails;
import com.logistics.supply.dto.*;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.buildEmail;
import static com.logistics.supply.util.Constants.*;

@RestController
@Slf4j
@RequestMapping(value = "/api")
@CrossOrigin(
    origins = {
      "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
      "http://localhost:4000"
    },
    allowedHeaders = "*")
public class ProcurementController {

  @Autowired private final EmailSender emailSender;
  @Autowired EmployeeService employeeService;
  @Autowired RequestItemService requestItemService;
  @Autowired ProcurementService procurementService;
  @Autowired LocalPurchaseOrderService localPurchaseOrderService;
  @Autowired SupplierService supplierService;
  @Autowired GeneratedQuoteService generatedQuoteService;

  @Autowired
  public ProcurementController(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @PutMapping(value = "/procurement/requestItem/procurementDetails")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> addProcurementInfo(
      Authentication authentication, @RequestBody @Valid ProcurementDTO procurementDTO) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    Optional<RequestItem> item =
        requestItemService.findById(procurementDTO.getRequestItem().getId());
    if (!item.isPresent()) return failedResponse("REQUEST_ITEM_NOT_FOUND");
    try {
      System.out.println("Trying to endorse after checking conditions");
      if (item.get().getEndorsement().equals(EndorsementStatus.ENDORSED)
          && item.get().getStatus().equals(RequestStatus.PENDING)
          && Objects.isNull(item.get().getSuppliedBy())) {
        System.out.println("Request can now be accessed for procurement details");
        RequestItem result =
            procurementService.assignProcurementDetails(item.get(), procurementDTO);
        requestItemService.saveRequest(item.get(), employee, RequestStatus.PENDING);
        if (Objects.isNull(result)) return failedResponse("UPDATE_REQUEST_ITEM_FAILED");
        Employee generalManager =
            employeeService.getGeneralManager(EmployeeRole.ROLE_GENERAL_MANAGER.ordinal());
        if (Objects.nonNull(generalManager)) {
          String emailContent =
              buildEmail(
                  generalManager.getLastName(),
                  REQUEST_PENDING_APPROVAL_LINK,
                  REQUEST_PENDING_APPROVAL_TITLE,
                  REQUEST_APPROVAL_MAIL);
          String generalManagerEmail = generalManager.getEmail();
          emailSender.sendMail(
              generalManagerEmail, EmailType.GENERAL_MANAGER_APPROVAL_MAIL, emailContent);
        }
        ResponseDTO response =
            new ResponseDTO("PROCUREMENT_DETAILED_ADDED_SUCCESSFULLY", SUCCESS, result);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("ADD_PROCUREMENT_DETAIL_FAILED");
  }

  @PutMapping(value = "/procurement/assignSuppliers/requestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> addSuppliersToRequestItem(
      @RequestBody MappingSuppliersAndRequestItemsDTO mappingDTO) {

    Set<RequestItem> items =
        mappingDTO.getRequestItems().stream()
            .filter(i -> requestItemService.existById(i.getId()))
            .map(r -> requestItemService.findById(r.getId()).get())
            .collect(Collectors.toSet());

    Set<Supplier> suppliers =
        mappingDTO.getSuppliers().stream()
            .map(s -> supplierService.findById(s.getId()))
            .collect(Collectors.toSet());

    Set<RequestItem> mappedRequests = procurementService.assignRequestToSupplier(suppliers, items);

    if (mappedRequests.size() > 0) {
      ResponseDTO response = new ResponseDTO("UPDATE_SUCCESSFUL", SUCCESS, mappedRequests);
      return ResponseEntity.ok(response);
    }
    return failedResponse("UPDATE_FAILED");
  }

  @PutMapping(value = "/procurement/setSuppliedBy/requestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> assignSupplierForRequestItems(
      @Valid @RequestBody SetSupplierDTO suppliedBy) {

    LocalPurchaseOrder lpo = procurementService.assignDetailsForMultipleItems(suppliedBy);
    if (Objects.nonNull(lpo)) {
      ResponseDTO response = new ResponseDTO("SUPPLIER_ASSIGNED", SUCCESS, lpo);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FAILED_TO_ASSIGN_SUPPLIER");
  }

  @GetMapping(value = "/procurement/localPurchaseOrders")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> findAllLPOS() {
    List<LocalPurchaseOrder> lpos = localPurchaseOrderService.findAll();
    if (!lpos.isEmpty()) {
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, lpos);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_FAILED");
  }

  @GetMapping(value = "/procurement/localPurchaseOrders/supplier/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> findLPOBySupplier(@PathVariable("supplierId") int supplierId) {
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
    if (!supplier.isPresent()) return failedResponse("SUPPLIER_NOT_FOUND");
    List<LocalPurchaseOrder> lpos = localPurchaseOrderService.findLpoBySupplier(supplierId);
    if (!lpos.isEmpty()) {
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, lpos);
      return ResponseEntity.ok(response);
    }
    return failedResponse("NO_LPO_EXIST_FOR_SUPPLIER");
  }

  @GetMapping(value = "/procurement/localPurchaseOrders/{lpoId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> findLPOById(@PathVariable("lpoId") int lpoId) {
    LocalPurchaseOrder lpo = localPurchaseOrderService.findLpoById(lpoId);
    if (Objects.nonNull(lpo)) {
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, lpo);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_FAILED");
  }

  @GetMapping(value = "/procurement/endorsedItemsWithMultipleSuppliers")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> findEndorsedItemsWithMultipleSuppliers() {
    List<RequestItem> items = new ArrayList<>();
    items.addAll(requestItemService.getEndorsedItemsWithAssignedSuppliers());
    if (!items.isEmpty()) {
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_FAILED");
  }

  @GetMapping(value = "/procurement/endorsedItemsWithSupplierId/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> findRequestItemsBySupplierId(
      @PathVariable("supplierId") int supplierId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemService.findRequestItemsForSupplier(supplierId));
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }


  @GetMapping(value = "/document/lpo/{lpoId}")
  public void getLpoDocumentInBrowser(
      @PathVariable("lpoId") int lpoId, HttpServletResponse response) throws Exception {
    System.out.println("lpoId print ");
    LocalPurchaseOrder lpo = this.localPurchaseOrderService.findLpoById(lpoId);
    if (Objects.isNull(lpo)) System.out.println("lpo does not exist");

    try {
      File file = this.localPurchaseOrderService.generateLPOPdf(lpoId);

      if (Objects.isNull(file)) System.out.println("something wrong somewhere");

      String mimeType = URLConnection.guessContentTypeFromName(file.getName());
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setHeader(
          "Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

      response.setContentLength((int) file.length());

      InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

      FileCopyUtils.copy(inputStream, response.getOutputStream());

    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  @GetMapping(value = "/requestItems/quotationsWithoutDocs")
  public ResponseEntity<?> findRequestItemsWithoutDocsInQuotation() {

    List<RequestItem> items = requestItemService.findRequestItemsWithoutDocInQuotation();
    if (!items.isEmpty()) {
      ResponseDTO response =
          new ResponseDTO("FETCH_QUOTATIONS_WITHOUT_DOCUMENTS_SUCCESSFUL", SUCCESS, items);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_FAILED");
  }

  @PutMapping(value = "/requestItems/updateRequestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> updateRequestItems(@RequestBody RequestItemListDTO requestItems) {
    try {
      Set<RequestItem> result =
          requestItems.getItems().stream()
              .filter(
                  r ->
                      (Objects.nonNull(r.getUnitPrice())
                          && Objects.nonNull(r.getRequestCategory())
                          && Objects.nonNull(r.getSuppliedBy())))
              .map(
                  i -> {
                    RequestItem item = requestItemService.findById(i.getId()).get();
                    item.setSuppliedBy(i.getSuppliedBy());
                    item.setUnitPrice(i.getUnitPrice());
                    item.setRequestCategory(i.getRequestCategory());
                    item.setStatus(RequestStatus.PROCESSED);
                    double totalPrice =
                        Double.parseDouble(String.valueOf(i.getUnitPrice())) * i.getQuantity();
                    item.setTotalPrice(BigDecimal.valueOf(totalPrice));
                    return requestItemService.saveRequestItem(item);
                  })
              .collect(Collectors.toSet());
      if (result.size() > 0) {
        LocalPurchaseOrder lpo = new LocalPurchaseOrder();
        lpo.setDeliveryDate(requestItems.getDeliveryDate());
        lpo.setComment("");
        lpo.setRequestItems(result);
        lpo.setSupplierId(result.stream().findFirst().get().getSuppliedBy());
        LocalPurchaseOrder newLpo = localPurchaseOrderService.saveLPO(lpo);
        if (Objects.nonNull(newLpo)) {
          ResponseDTO response = new ResponseDTO("UPDATE_SUCCESSFUL", SUCCESS, newLpo);
          return ResponseEntity.ok(response);
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("UPDATE_FAILED");
  }

  @PostMapping(value = "/quotations/generateQuoteForSupplier")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public void generateQuoteForSupplier(
      @RequestBody GeneratedQuoteDTO request, HttpServletResponse response) {
    try {
      File file = generatedQuoteService.createQuoteForUnregisteredSupplier(request);
      if (Objects.isNull(file)) System.out.println("something wrong somewhere");

      String mimeType = URLConnection.guessContentTypeFromName(file.getName());
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setHeader(
          "Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

      response.setContentLength((int) file.length());

      InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

      FileCopyUtils.copy(inputStream, response.getOutputStream());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  @GetMapping(value = "/requestItems/generateRequestListForSupplier/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public void generateRequestListForSupplier(
      @PathVariable("supplierId") int supplierId, HttpServletResponse response) {
    try {
      AppUserDetails principal =
          (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      Employee employee = principal.getEmployee();
      File file = requestItemService.generateRequestListForSupplier(supplierId, employee);
      if (Objects.isNull(file)) System.out.println("something wrong somewhere");

      String mimeType = URLConnection.guessContentTypeFromName(file.getName());
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setHeader(
          "Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

      response.setContentLength((int) file.length());

      InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

      FileCopyUtils.copy(inputStream, response.getOutputStream());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
