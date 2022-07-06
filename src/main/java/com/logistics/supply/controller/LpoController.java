package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.errorhandling.GeneralException;
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
import org.springframework.transaction.annotation.Transactional;
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

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;
import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LpoController {
  private final RequestItemService requestItemService;
  private final EmployeeService employeeService;
  private final LocalPurchaseOrderDraftService localPurchaseOrderDraftService;
  private final LocalPurchaseOrderService localPurchaseOrderService;
  private final QuotationService quotationService;

  @Transactional(rollbackFor = Exception.class)
  @PostMapping(value = "/localPurchaseOrderDrafts")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createLPODraft(@Valid @RequestBody RequestItemListDTO requestItems) {
    Set<RequestItem> result =
        requestItemService.assignProcurementDetailsToItems(requestItems.getItems());
    if (result.isEmpty()) return ResponseDTO.wrapErrorResult("MISSING REQUEST ITEMS FOR LPO");
    LocalPurchaseOrderDraft lpo = new LocalPurchaseOrderDraft();
    lpo.setDeliveryDate(requestItems.getDeliveryDate());
    lpo.setRequestItems(result);
    lpo.setSupplierId(result.stream().findFirst().get().getSuppliedBy());
    Quotation quotation = quotationService.findById(requestItems.getQuotationId());
    lpo.setQuotation(quotation);
    LocalPurchaseOrderDraft newLpo = localPurchaseOrderDraftService.saveLPO(lpo);
    return ResponseDTO.wrapSuccessResult(newLpo, "LPO DRAFT CREATED SUCCESSFULLY");
  }

  @PostMapping(value = "/localPurchaseOrders")
  @Transactional(rollbackFor = Exception.class)
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

      return ResponseDTO.wrapSuccessResult(newLpo, "LPO CREATED SUCCESSFULLY");

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("LPO CREATION FAILED");
  }

  @Operation(summary = "Get list of LPO by parameters", tags = "LOCAL PURCHASE ORDER")
  @GetMapping(value = "/localPurchaseOrderDrafts")
  public ResponseEntity<?> getLpoList(
          @RequestParam(defaultValue = "false", required = false) Boolean draftAwaitingApproval, @RequestParam Optional<Boolean> lpoReview) {
    if (draftAwaitingApproval) {
      List<LocalPurchaseOrderDraft> lpos =
          localPurchaseOrderDraftService.findDraftAwaitingApproval();
      return ResponseDTO.wrapSuccessResult(lpos, "FETCH DRAFT AWAITING APPROVAL SUCCESSFUL");
    }

    if(lpoReview.isPresent() && lpoReview.get()) {
      List<LocalPurchaseOrderDraft> lpoForReview =
              localPurchaseOrderDraftService.findDraftAwaitingApproval();
      lpoForReview.removeIf(l -> l.getQuotation().isReviewed() == true);
      return ResponseDTO.wrapSuccessResult(lpoForReview, FETCH_SUCCESSFUL);
    }

    List<LocalPurchaseOrderDraft> lpos = localPurchaseOrderDraftService.findAll();
    return ResponseDTO.wrapSuccessResult(lpos, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/localPurchaseOrderDrafts/{id}")
  public ResponseEntity<?> getLpoDraft(@PathVariable int id) throws GeneralException {
    LocalPurchaseOrderDraft lpoById = localPurchaseOrderDraftService.findLpoById(id);
    return ResponseDTO.wrapSuccessResult(lpoById, FETCH_SUCCESSFUL);
  }

  @Operation(summary = "Get LPO by the id", tags = "LOCAL PURCHASE ORDER")
  @GetMapping(value = "/localPurchaseOrders/{lpoId}")
  public ResponseEntity<?> getLPOById(@PathVariable("lpoId") int lpoId) throws GeneralException {
    LocalPurchaseOrder lpo = localPurchaseOrderService.findLpoById(lpoId);
    ResponseDTO response = new ResponseDTO("FETCH LPO SUCCESSFUL", SUCCESS, lpo);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/localPurchaseOrders")
  public ResponseEntity<?> listLPO(
      @RequestParam(defaultValue = "false", required = false) Boolean lpoWithoutGRN,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      Authentication authentication) {
    if (lpoWithoutGRN) {
      List<LocalPurchaseOrder> lpo = localPurchaseOrderService.findLpoWithoutGRN();
      return ResponseDTO.wrapSuccessResult(lpo, FETCH_SUCCESSFUL);
    }
    Page<LocalPurchaseOrder> localPurchaseOrders =
        localPurchaseOrderService.findAll(pageNo, pageSize);
    if (localPurchaseOrders != null)
      return PagedResponseDTO.wrapSuccessResult(localPurchaseOrders, FETCH_SUCCESSFUL);
    return notFound("NO LPO FOUND");
  }

  @GetMapping(value = "/localPurchaseOrders/supplier/{supplierId}")
  public ResponseEntity<?> getLPOBySupplier(@PathVariable("supplierId") int supplierId)
      throws GeneralException {
    List<LocalPurchaseOrderDraft> lpos =
        localPurchaseOrderDraftService.findLpoBySupplier(supplierId);
    return ResponseDTO.wrapSuccessResult(lpos, FETCH_SUCCESSFUL);
  }

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
}
