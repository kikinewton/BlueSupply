package com.logistics.supply.controller;

import com.logistics.supply.dto.LpoDTO;
import com.logistics.supply.dto.RequestItemListDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.IdentifierUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LpoController {

  final RequestItemService requestItemService;
  final EmployeeService employeeService;
  final LocalPurchaseOrderDraftService localPurchaseOrderDraftService;
  final LocalPurchaseOrderService localPurchaseOrderService;
  final QuotationService quotationService;
  final SupplierService supplierService;

  @Operation(summary = "Add LPO draft ", tags = "Procurement")
  @PostMapping(value = "/localPurchaseOrderDraft")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createLPODraft(@Valid @RequestBody RequestItemListDTO requestItems) {
    try {
      Set<RequestItem> result =
          requestItemService.assignProcurementDetailsToItems(requestItems.getItems());
      if (result.isEmpty()) return failedResponse("MISSING_REQUEST_ITEMS_FOR_LPO");
      LocalPurchaseOrderDraft lpo = new LocalPurchaseOrderDraft();
      lpo.setDeliveryDate(requestItems.getDeliveryDate());
      lpo.setRequestItems(result);
      lpo.setSupplierId(result.stream().findFirst().get().getSuppliedBy());
      Quotation quotation = quotationService.findById(requestItems.getQuotationId());
      lpo.setQuotation(quotation);


      LocalPurchaseOrderDraft newLpo = localPurchaseOrderDraftService.saveLPO(lpo);
      if (Objects.nonNull(newLpo)) {
//        AddLPOEvent lpoEvent = new AddLPOEvent(this, newLpo);

        ResponseDTO response = new ResponseDTO("LPO_DRAFT_CREATED_SUCCESSFULLY", SUCCESS, newLpo);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("LPO_CREATION_FAILED");
  }

  @Operation(summary = "Add LPO draft ", tags = "Procurement")
  @PostMapping(value = "/localPurchaseOrder")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createLPO(@Valid @RequestBody LpoDTO lpoDto) {
    try {
      LocalPurchaseOrderDraft draft = localPurchaseOrderDraftService.findLpoById(lpoDto.getDraftId());
      LocalPurchaseOrder lpo = new LocalPurchaseOrder();
      lpo.setRequestItems(lpo.getRequestItems());
      lpo.setSupplierId(draft.getSupplierId());
      lpo.setQuotation(draft.getQuotation());
      String count = String.valueOf(localPurchaseOrderService.count());
      String department = lpo.getRequestItems().stream().findAny().get().getUserDepartment().getName();
      String ref = IdentifierUtil.idHandler("LPO", department, count);
      lpo.setLpoRef(ref);

      LocalPurchaseOrder newLpo = localPurchaseOrderService.saveLPO(lpo);
      if (Objects.nonNull(newLpo)) {
        ResponseDTO response = new ResponseDTO("LPO_CREATED_SUCCESSFULLY", SUCCESS, newLpo);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("LPO_CREATION_FAILED");
  }


  @Operation(summary = "Get list of LPO by parameters", tags = "LOCAL_PURCHASE_ORDER")
  @GetMapping(value = "/localPurchaseOrders")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> getAllLPOS(
      @RequestParam(defaultValue = "false", required = false) Boolean withGRN,
      @RequestParam(defaultValue = "false", required = false) Boolean all) {
    if (all) {
      List<LocalPurchaseOrderDraft> lpos = localPurchaseOrderDraftService.findAll();
      ResponseDTO response = new ResponseDTO("FETCH_ALL_LPO_SUCCESSFUL", SUCCESS, lpos);
      return ResponseEntity.ok(response);
    }
    if (withGRN) {
      List<LocalPurchaseOrderDraft> lpos = localPurchaseOrderDraftService.findLpoLinkedToGRN();
      ResponseDTO response = new ResponseDTO("FETCH_LPO_WITH_GRN_SUCCESSFUL", SUCCESS, lpos);
      return ResponseEntity.ok(response);
    }
    List<LocalPurchaseOrderDraft> lpos = localPurchaseOrderDraftService.findLpoWithoutGRN();
    ResponseDTO response = new ResponseDTO("FETCH_LPO_WITHOUT_SUCCESSFUL", SUCCESS, lpos);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get LPO by the id", tags = "LOCAL_PURCHASE_ORDER")
  @GetMapping(value = "/localPurchaseOrders/{lpoId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> getLPOById(@PathVariable("lpoId") int lpoId) {
    LocalPurchaseOrderDraft lpo = localPurchaseOrderDraftService.findLpoById(lpoId);
    if (Objects.nonNull(lpo)) {
      ResponseDTO response = new ResponseDTO("FETCH_LPO_SUCCESSFUL", SUCCESS, lpo);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_FAILED");
  }

  @Operation(summary = "Get LPO by the lpo Ref", tags = "LOCAL_PURCHASE_ORDER")
  @GetMapping(value = "/localPurchaseOrders/{lpoRef}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> getLPOById(@PathVariable("lpoRef") String lpoRef) {
    LocalPurchaseOrder lpo = localPurchaseOrderService.findLpoByRef(lpoRef);
    if (Objects.nonNull(lpo)) {
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, lpo);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_FAILED");
  }

  @Operation(summary = "Get the LPO's for the specified supplier", tags = "LOCAL_PURCHASE_ORDER")
  @GetMapping(value = "/localPurchaseOrders/supplier/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> getLPOBySupplier(@PathVariable("supplierId") int supplierId) {
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
    if (!supplier.isPresent()) return failedResponse("SUPPLIER_NOT_FOUND");
    List<LocalPurchaseOrderDraft> lpos = localPurchaseOrderDraftService.findLpoBySupplier(supplierId);

    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, lpos);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Download the LPO document", tags = "LOCAL_PURCHASE_ORDER")
  @GetMapping(value = "/localPurchaseOrders/{lpoId}/download")
  public void downloadLpoDocumentInBrowser(
      @PathVariable("lpoId") int lpoId, HttpServletResponse response) throws Exception {
    LocalPurchaseOrderDraft lpo = this.localPurchaseOrderDraftService.findLpoById(lpoId);
    if (Objects.isNull(lpo)) log.error("lpo does not exist");

    try {
      File file = this.localPurchaseOrderService.generateLPOPdf(lpoId);
      if (Objects.isNull(file)) log.error("LPO file output is null");

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
      log.error(e.toString());
    }
  }

  public ResponseEntity<?> reviewLPOPerDepartment(Authentication authentication) {
    Department department =
        employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
    //    localPurchaseOrderService.findAll().stream().map(x -> x.getRequestItems().stream().)
    return null;
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
