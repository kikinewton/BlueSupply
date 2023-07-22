package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.LocalPurchaseOrderDraft;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.LocalPurchaseOrderDraftService;
import com.logistics.supply.service.LocalPurchaseOrderService;
import com.logistics.supply.util.AuthHelper;
import com.logistics.supply.util.Constants;
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
import org.springframework.validation.annotation.Validated;
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

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class LpoController {

    private final EmployeeService employeeService;
    private final LocalPurchaseOrderDraftService localPurchaseOrderDraftService;
    private final LocalPurchaseOrderService localPurchaseOrderService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping(value = "/api/localPurchaseOrderDrafts")
    @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
    public ResponseEntity<ResponseDto<LocalPurchaseOrderDraft>> createLPODraft(
            @Valid @RequestBody RequestItemListDTO requestItems) {

        LocalPurchaseOrderDraft newLpo = localPurchaseOrderDraftService.createLPODraft(requestItems);
        return ResponseDto.wrapSuccessResult(newLpo, "LPO DRAFT CREATED SUCCESSFULLY");
    }

    @PostMapping(value = "/api/localPurchaseOrders")
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
    public ResponseEntity<ResponseDto<LocalPurchaseOrder>> createLPO(@Valid @RequestBody LpoDTO lpoDto) {

        LocalPurchaseOrder localPurchaseOrder = localPurchaseOrderDraftService.createLpoFromDraft(lpoDto);
        return ResponseDto.wrapSuccessResult(localPurchaseOrder, "LPO CREATED SUCCESSFULLY");
    }

    @Operation(summary = "Get list of LPO by parameters", tags = "LOCAL PURCHASE ORDER")
    @GetMapping(value = "/api/localPurchaseOrderDrafts")
    public ResponseEntity<PagedResponseDto<Page<LpoDraftDto>>> getLpoList(
            @RequestParam(defaultValue = "false", required = false) Boolean draftAwaitingApproval,
            Authentication authentication,
            @RequestParam(name = "underReview") Optional<Boolean> lpoReview,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "300") int pageSize) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        if (draftAwaitingApproval) {
            Page<LpoDraftDto> lpos = localPurchaseOrderDraftService.findDraftDtoAwaitingApproval(pageable);
            return PagedResponseDto.wrapSuccessResult(lpos, Constants.FETCH_SUCCESSFUL);
        }

        if (lpoReview.isPresent()
            && lpoReview.get()
            && AuthHelper.checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {

            Employee employeeByEmail = employeeService.findEmployeeByEmail(authentication.getName());
            log.info("Fetch lpo draft under review by HOD: {}", employeeByEmail.getEmail());
            int departmentId = employeeByEmail.getDepartment().getId();
            Page<LpoDraftDto> lpoForReview =
                    localPurchaseOrderDraftService.findDraftDtoAwaitingApprovalByHod(departmentId, pageable);
            lpoForReview.getContent().removeIf(l -> l.getQuotation().isReviewed());

            return PagedResponseDto.wrapSuccessResult(lpoForReview, Constants.FETCH_SUCCESSFUL);
        }

        Page<LpoDraftDto> lpoDraftDtos = localPurchaseOrderDraftService.findAll(pageable)
                .map(LpoDraftDto::toDto);

        return PagedResponseDto.wrapSuccessResult(lpoDraftDtos, Constants.FETCH_SUCCESSFUL);
    }

    @GetMapping(value = "/api/localPurchaseOrderDrafts/{id}")
    public ResponseEntity<ResponseDto<LocalPurchaseOrderDraft>> getLpoDraft(@PathVariable int id) {

        LocalPurchaseOrderDraft purchaseOrderDraft = localPurchaseOrderDraftService.findLpoById(id);
        return ResponseDto.wrapSuccessResult(purchaseOrderDraft, Constants.FETCH_SUCCESSFUL);
    }

    @Operation(summary = "Get LPO by the id", tags = "LOCAL PURCHASE ORDER")
    @GetMapping(value = "/api/localPurchaseOrders/{lpoId}")
    public ResponseEntity<ResponseDto<LocalPurchaseOrder>> getLPOById(
            @PathVariable("lpoId") int lpoId) {

        LocalPurchaseOrder localPurchaseOrder = localPurchaseOrderService.findLpoById(lpoId);
        return ResponseDto.wrapSuccessResult(localPurchaseOrder, Constants.FETCH_SUCCESSFUL);
    }

    @GetMapping(value = "/api/localPurchaseOrders")
    public ResponseEntity<?> listLPO(
            @RequestParam(defaultValue = "false", required = false) Boolean lpoWithoutGRN,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String supplierName) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        if (lpoWithoutGRN) {
            Page<LpoMinorDto> lpo = localPurchaseOrderService.findLpoDtoWithoutGRN(pageable);
            return PagedResponseDto.wrapSuccessResult(lpo, Constants.FETCH_SUCCESSFUL);
        }

        if (StringUtils.hasText(supplierName)) {

            Page<LocalPurchaseOrder> lpoBySupplierName =
                    localPurchaseOrderService.findLpoBySupplierName(supplierName, pageable);
            return PagedResponseDto.wrapSuccessResult(lpoBySupplierName, Constants.FETCH_SUCCESSFUL);
        }

        Page<LocalPurchaseOrder> localPurchaseOrders =
                localPurchaseOrderService.findAll(pageable);
        return PagedResponseDto.wrapSuccessResult(localPurchaseOrders, Constants.FETCH_SUCCESSFUL);
    }

    @GetMapping(value = "/api/localPurchaseOrders/supplier/{supplierId}")
    public ResponseEntity<ResponseDto<List<LocalPurchaseOrderDraft>>> getLPOBySupplier(
            @PathVariable("supplierId") int supplierId) {

        List<LocalPurchaseOrderDraft> localPurchaseOrderDrafts =
                localPurchaseOrderDraftService.findLpoBySupplier(supplierId);
        return ResponseDto.wrapSuccessResult(localPurchaseOrderDrafts, Constants.FETCH_SUCCESSFUL);
    }

    @GetMapping(value = "/res/localPurchaseOrders/{lpoId}/download")
    public void downloadLpoDocumentInBrowser(
            @PathVariable("lpoId") int lpoId, HttpServletResponse response) {

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
