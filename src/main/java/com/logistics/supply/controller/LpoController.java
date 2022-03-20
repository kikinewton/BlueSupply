package com.logistics.supply.controller;

import com.logistics.supply.dto.LpoDTO;
import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.dto.RequestItemListDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.IdentifierUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

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
  @PostMapping(value = "/localPurchaseOrderDrafts")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createLPODraft(@Valid @RequestBody RequestItemListDTO requestItems) {
    try {
      Set<RequestItem> result =
          requestItemService.assignProcurementDetailsToItems(requestItems.getItems());
      if (result.isEmpty()) return failedResponse("MISSING REQUEST ITEMS FOR LPO");
      LocalPurchaseOrderDraft lpo = new LocalPurchaseOrderDraft();
      lpo.setDeliveryDate(requestItems.getDeliveryDate());
      lpo.setRequestItems(result);
      lpo.setSupplierId(result.stream().findFirst().get().getSuppliedBy());
      Quotation quotation = quotationService.findById(requestItems.getQuotationId());
      lpo.setQuotation(quotation);

      LocalPurchaseOrderDraft newLpo = localPurchaseOrderDraftService.saveLPO(lpo);
      if (Objects.nonNull(newLpo)) {
        //        AddLPOEvent lpoEvent = new AddLPOEvent(this, newLpo);

        ResponseDTO response = new ResponseDTO("LPO DRAFT CREATED SUCCESSFULLY", SUCCESS, newLpo);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("LPO CREATION FAILED");
  }

  @Operation(summary = "Add LPO ", tags = "PROCUREMENT")
  @PostMapping(value = "/localPurchaseOrders")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createLPO(@Valid @RequestBody LpoDTO lpoDto) {
    try {
      LocalPurchaseOrderDraft draft =
          localPurchaseOrderDraftService.findLpoById(lpoDto.getDraftId());
      LocalPurchaseOrder lpo = new LocalPurchaseOrder();
      Employee generalManager = employeeService.getGeneralManager();
      lpo.setApprovedBy(generalManager);
      Set<RequestItem> items =
          draft.getRequestItems().stream()
              .filter(i -> i.getApproval() == RequestApproval.APPROVED)
              .collect(Collectors.toSet());
      lpo.setRequestItems(items);
      lpo.setSupplierId(draft.getSupplierId());
      lpo.setQuotation(draft.getQuotation());
      String count = String.valueOf(localPurchaseOrderService.count());
      String department =
          lpo.getRequestItems().stream().findAny().get().getUserDepartment().getName();
      String ref = IdentifierUtil.idHandler("LPO", department, count);
      lpo.setLpoRef(ref);
      lpo.setIsApproved(true);
      lpo.setDeliveryDate(draft.getDeliveryDate());
      lpo.setLocalPurchaseOrderDraft(draft);
      lpo.setDepartment(draft.getDepartment());

      LocalPurchaseOrder newLpo = localPurchaseOrderService.saveLPO(lpo);
      if (Objects.nonNull(newLpo)) {
        ResponseDTO response = new ResponseDTO("LPO CREATED SUCCESSFULLY", SUCCESS, newLpo);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("LPO CREATION FAILED");
  }

  @Operation(summary = "Get list of LPO by parameters", tags = "LOCAL PURCHASE ORDER")
  @GetMapping(value = "/localPurchaseOrderDrafts")
  public ResponseEntity<?> getAllLPOS(
      @RequestParam(defaultValue = "false", required = false) Boolean draftAwaitingApproval) {
    if (draftAwaitingApproval) {
      List<LocalPurchaseOrderDraft> lpos =
          localPurchaseOrderDraftService.findDraftAwaitingApproval();
      ResponseDTO response =
          new ResponseDTO("FETCH DRAFT AWAITING APPROVAL SUCCESSFUL", SUCCESS, lpos);
      return ResponseEntity.ok(response);
    }

    List<LocalPurchaseOrderDraft> lpos = localPurchaseOrderDraftService.findAll();
    ResponseDTO response = new ResponseDTO("FETCH ALL LPO DRAFT SUCCESSFUL", SUCCESS, lpos);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get LPO by the id", tags = "LOCAL PURCHASE ORDER")
  @GetMapping(value = "/localPurchaseOrders/{lpoId}")
  public ResponseEntity<?> getLPOById(@PathVariable("lpoId") int lpoId) {
    LocalPurchaseOrder lpo = localPurchaseOrderService.findLpoById(lpoId);
    if (Objects.nonNull(lpo)) {
      ResponseDTO response = new ResponseDTO("FETCH LPO SUCCESSFUL", SUCCESS, lpo);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH FAILED");
  }

  @Operation(summary = "Get LPOs", tags = "LOCAL PURCHASE ORDER")
  @GetMapping(value = "/localPurchaseOrders")
  public ResponseEntity<?> listLPO(
      @RequestParam(defaultValue = "false", required = false) Boolean lpoWithoutGRN,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      Authentication authentication) {
    if (lpoWithoutGRN) {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      Department department = employee.getDepartment();
      List<LocalPurchaseOrder> lpo = localPurchaseOrderService.findLpoWithoutGRN(department);
      ResponseDTO response = new ResponseDTO("FETCH LPO WITHOUT GRN SUCCESSFUL", SUCCESS, lpo);
      return ResponseEntity.ok(response);
    }
    Page<LocalPurchaseOrder> localPurchaseOrders =
        localPurchaseOrderService.findAll(pageNo, pageSize);
    if (localPurchaseOrders != null) return pagedResult(localPurchaseOrders);
    return notFound("NO LPO FOUND");
  }

  @Operation(summary = "Get the LPO's for the specified supplier", tags = "LOCAL PURCHASE ORDER")
  @GetMapping(value = "/localPurchaseOrders/supplier/{supplierId}")
  public ResponseEntity<?> getLPOBySupplier(@PathVariable("supplierId") int supplierId) {
    Optional<Supplier> supplier = supplierService.findBySupplierId(supplierId);
    if (!supplier.isPresent()) return failedResponse("SUPPLIER NOT FOUND");
    List<LocalPurchaseOrderDraft> lpos =
        localPurchaseOrderDraftService.findLpoBySupplier(supplierId);

    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, lpos);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Download the LPO document", tags = "LOCAL PURCHASE ORDER")
  @GetMapping(value = "/localPurchaseOrders/{lpoId}/download")
  public void downloadLpoDocumentInBrowser(
      @PathVariable("lpoId") int lpoId, HttpServletResponse response) throws Exception {
    LocalPurchaseOrder lpo = this.localPurchaseOrderService.findLpoById(lpoId);
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

  private ResponseEntity<?> pagedResult(Page<LocalPurchaseOrder> lpo) {
    PagedResponseDTO.MetaData metaData =
        new PagedResponseDTO.MetaData(
            lpo.getNumberOfElements(),
            lpo.getPageable().getPageSize(),
            lpo.getNumber(),
            lpo.getTotalPages());
    PagedResponseDTO response =
        new PagedResponseDTO("FETCH SUCCESSFUL", SUCCESS, metaData, lpo.getContent());
    return ResponseEntity.ok(response);
  }
}
