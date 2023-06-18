package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.*;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.LocalPurchaseOrderDraftService;
import com.logistics.supply.service.LocalPurchaseOrderService;
import com.logistics.supply.util.AuthHelper;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import com.logistics.supply.util.IdentifierUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
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

@Slf4j
@RestController
@RequiredArgsConstructor
public class LpoController {
  private final EmployeeService employeeService;
  private final LocalPurchaseOrderDraftService localPurchaseOrderDraftService;
  private final LocalPurchaseOrderService localPurchaseOrderService;

  @Transactional(rollbackFor = Exception.class)
  @PostMapping(value = "/api/localPurchaseOrderDrafts")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createLPODraft(@Valid @RequestBody RequestItemListDTO requestItems) {
    LocalPurchaseOrderDraft newLpo = localPurchaseOrderDraftService.createLPODraft(requestItems);
    return ResponseDto.wrapSuccessResult(newLpo, "LPO DRAFT CREATED SUCCESSFULLY");
  }

  @PostMapping(value = "/api/localPurchaseOrders")
  @Transactional(rollbackFor = Exception.class)
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> createLPO(@Valid @RequestBody LpoDTO lpoDto) {

    LocalPurchaseOrderDraft draft = localPurchaseOrderDraftService.findLpoById(lpoDto.getDraftId());
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

    return ResponseDto.wrapSuccessResult(newLpo, "LPO CREATED SUCCESSFULLY");
  }

  @Operation(summary = "Get list of LPO by parameters", tags = "LOCAL PURCHASE ORDER")
  @GetMapping(value = "/api/localPurchaseOrderDrafts")
  public ResponseEntity<?> getLpoList(

      Authentication authentication,
      @RequestParam(defaultValue = "false", required = false) Boolean draftAwaitingApproval,
      @RequestParam(name = "underReview") Optional<Boolean> lpoReview) {
    if (draftAwaitingApproval) {
      List<LpoDraftDto> lpos = localPurchaseOrderDraftService.findDraftDtoAwaitingApproval();
      return ResponseDto.wrapSuccessResult(lpos, Constants.FETCH_SUCCESSFUL);
    }

    if (lpoReview.isPresent()
        && lpoReview.get()
        && AuthHelper.checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      Employee employeeByEmail = employeeService.findEmployeeByEmail(authentication.getName());
      log.info("Fetch lpo draft under review by HOD: {}", employeeByEmail);
      Integer departmentId = employeeByEmail.getDepartment().getId();
      List<LpoDraftDto> lpoForReview =
          localPurchaseOrderDraftService.findDraftDtoAwaitingApprovalByHod(departmentId);
      lpoForReview.removeIf(l -> l.getQuotation().isReviewed());

      return ResponseDto.wrapSuccessResult(lpoForReview, Constants.FETCH_SUCCESSFUL);
    }

    List<LocalPurchaseOrderDraft> lpos = localPurchaseOrderDraftService.findAll();
    return ResponseDto.wrapSuccessResult(lpos, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/api/localPurchaseOrderDrafts/{id}")
  public ResponseEntity<?> getLpoDraft(@PathVariable int id) {
    LocalPurchaseOrderDraft lpoById = localPurchaseOrderDraftService.findLpoById(id);
    return ResponseDto.wrapSuccessResult(lpoById, Constants.FETCH_SUCCESSFUL);
  }

  @Operation(summary = "Get LPO by the id", tags = "LOCAL PURCHASE ORDER")
  @GetMapping(value = "/api/localPurchaseOrders/{lpoId}")
  public ResponseEntity<?> getLPOById(@PathVariable("lpoId") int lpoId) {
    LocalPurchaseOrder lpo = localPurchaseOrderService.findLpoById(lpoId);
    return ResponseDto.wrapSuccessResult(lpo, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/api/localPurchaseOrders")
  public ResponseEntity<?> listLPO(
      @RequestParam(defaultValue = "false", required = false) Boolean lpoWithoutGRN,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) String supplierName) {
    if (lpoWithoutGRN) {
      List<LpoMinorDto> lpo = localPurchaseOrderService.findLpoDtoWithoutGRN();
      return ResponseDto.wrapSuccessResult(lpo, Constants.FETCH_SUCCESSFUL);
    }
    if (StringUtils.hasText(supplierName)) {
      Pageable pageable = PageRequest.of(pageNo, pageSize);
      List<LocalPurchaseOrder> lpoBySupplierName =
          localPurchaseOrderService.findLpoBySupplierName(supplierName, pageable);
      return ResponseDto.wrapSuccessResult(lpoBySupplierName, Constants.FETCH_SUCCESSFUL);
    }
    Page<LocalPurchaseOrder> localPurchaseOrders =
        localPurchaseOrderService.findAll(pageNo, pageSize);
    if (localPurchaseOrders != null)
      return PagedResponseDTO.wrapSuccessResult(localPurchaseOrders, Constants.FETCH_SUCCESSFUL);
    return Helper.notFound("NO LPO FOUND");
  }

  @GetMapping(value = "/api/localPurchaseOrders/supplier/{supplierId}")
  public ResponseEntity<?> getLPOBySupplier(@PathVariable("supplierId") int supplierId) {
    List<LocalPurchaseOrderDraft> lpos =
        localPurchaseOrderDraftService.findLpoBySupplier(supplierId);
    return ResponseDto.wrapSuccessResult(lpos, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/res/localPurchaseOrders/{lpoId}/download")
  public void downloadLpoDocumentInBrowser(
      @PathVariable("lpoId") int lpoId, HttpServletResponse response) throws Exception {
    try {
      File file = this.localPurchaseOrderService.generateLPOPdf(lpoId);
      if (Objects.isNull(file)) {
        log.error("LPO file output is null");
        return;
      }

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
