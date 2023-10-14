package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.Helper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;

@Slf4j
@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuotationController {

    private final GeneratedQuoteService generatedQuoteService;
    private final SupplierService supplierService;
    private final QuotationService quotationService;
    private final RequestItemService requestItemService;
    private final RequestDocumentService documentService;
    private final EmployeeService employeeService;


    @PostMapping(value = "/quotations")
    @PreAuthorize(
            "hasRole('ROLE_PROCUREMENT_OFFICER')" +
            "or hasRole('ROLE_PROCUREMENT_MANAGER')")
    public ResponseEntity<ResponseDto<Quotation>> createQuotation(
            @Valid @RequestBody CreateQuotationRequest quotationRequest) {

        Quotation savedQuotation = quotationService.createQuotation(quotationRequest);
        return ResponseDto.wrapSuccessResult(savedQuotation, "QUOTATION ASSIGNED TO REQUEST ITEMS");
    }

    @GetMapping(value = "/quotations/suppliers/{supplierId}")
    @PreAuthorize(
            "hasRole('ROLE_GENERAL_MANAGER') " +
            "or hasRole('ROLE_PROCUREMENT_OFFICER')")
    public ResponseEntity<ResponseDto<List<SupplierQuotationDto>>> getQuotationsBySupplier(
            @PathVariable("supplierId") int supplierId) {

        List<SupplierQuotationDto> supplierQuotation =
                quotationService.findSupplierQuotation(supplierId);
        return ResponseDto.wrapSuccessResult(supplierQuotation, "FETCHED QUOTATIONS BY SUPPLIER");
    }

    @GetMapping("/quotations/linkedToLpo")
    @PreAuthorize(
            "hasRole('ROLE_PROCUREMENT_OFFICER') " +
            "or hasRole('ROLE_PROCUREMENT_MANAGER') " +
            "or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseDto<List<QuotationAndRelatedRequestItemsDto>>> getQuotationsLinkedToLpo() {

        List<QuotationAndRelatedRequestItemsDto> quotationAndRelatedRequestItemsDtoList = quotationService
                .fetchQuotationLinkedToLpoWithRequestItems();
        return ResponseDto.wrapSuccessResult(
                quotationAndRelatedRequestItemsDtoList,
                "FETCH ALL QUOTATIONS");
    }

    @GetMapping("/quotations/notLinkedToLpo")
    @PreAuthorize(
            "hasRole('ROLE_PROCUREMENT_OFFICER')" +
            "or hasRole('ROLE_PROCUREMENT_MANAGER')" +
            "or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseDto<List<QuotationAndRelatedRequestItemsDto>>> getQuotationsNotLinkedToLpo() {

        List<QuotationAndRelatedRequestItemsDto> quotationAndRelatedRequestItemsDtoList = quotationService
                .fetchQuotationNotLinkedToLpoWithRequestItems();
        return ResponseDto.wrapSuccessResult(
                quotationAndRelatedRequestItemsDtoList,
                "FETCH ALL QUOTATIONS LINKED NOT LINKED TO LPO");
    }

    @GetMapping("/quotations/underReview")
    @PreAuthorize(
            "hasRole('ROLE_GENERAL_MANAGER') " +
            "or hasRole('ROLE_PROCUREMENT_OFFICER') " +
            "or hasRole('ROLE_PROCUREMENT_MANAGER') " +
            "or hasRole('ROLE_ADMIN') " +
            "or hasRole('ROLE_AUDITOR')" +
            "or hasRole('ROLE_HOD')")
    public ResponseEntity<ResponseDto<List<QuotationAndRelatedRequestItemsDto>>> getQuotationsUnderReview(
            Authentication authentication
    ) {
        Employee employee = employeeService.findEmployeeByEmail(authentication.getName());

        List<QuotationAndRelatedRequestItemsDto> quotationAndRelatedRequestItemsDtos = new ArrayList<>();

        if (Helper.hasRole(employee, EmployeeRole.ROLE_HOD)) {

            quotationAndRelatedRequestItemsDtos = quotationService
                    .fetchQuotationsUnderHodReviewWithRequestItems();
        }
        if (Helper.hasRole(employee, EmployeeRole.ROLE_AUDITOR)) {
            quotationAndRelatedRequestItemsDtos = quotationService.fetchQuotationsUnderAuditorReviewWithRequestItems();
        }

        return ResponseDto.wrapSuccessResult(quotationAndRelatedRequestItemsDtos, FETCH_SUCCESSFUL);
    }

    @Operation(summary = "Fetch approved quotations", tags = "QUOTATION")
    @GetMapping("/quotations/approved")
    @PreAuthorize(
            "hasRole('ROLE_GENERAL_MANAGER') " +
            "or hasRole('ROLE_PROCUREMENT_OFFICER') " +
            "or hasRole('ROLE_PROCUREMENT_MANAGER') " +
            "or hasRole('ROLE_ADMIN')")
    public ResponseEntity<PagedResponseDto<Page<Quotation>>> fetchApprovedQuotations(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "300", required = false) int pageSize,
            @RequestParam(required = false, name = "supplierName") String supplierName) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        if (StringUtils.hasText(supplierName)) {

            Supplier supplier = supplierService.findFirstByNameLike(supplierName);
            Page<Quotation> quotationLinkedToLPOBySupplier = quotationService
                    .findQuotationLinkedToLPOBySupplier(supplier, pageable);

            return PagedResponseDto.wrapSuccessResult(quotationLinkedToLPOBySupplier, FETCH_SUCCESSFUL);
        }

        Page<Quotation> allQuotationsLinkedToLPO =
                quotationService.findAllQuotationsLinkedToLPO(pageable);
        return PagedResponseDto.wrapSuccessResult(allQuotationsLinkedToLPO, FETCH_SUCCESSFUL);
    }

    @Operation(summary = "Fetch quotations", tags = "QUOTATION")
    @GetMapping("/quotations")
    @PreAuthorize(
            "hasRole('ROLE_GENERAL_MANAGER') " +
            "or hasRole('ROLE_PROCUREMENT_OFFICER') " +
            "or hasRole('ROLE_PROCUREMENT_MANAGER') " +
            "or hasRole('ROLE_AUDITOR')" +
            "or hasRole('ROLE_ADMIN')")
    public ResponseEntity<PagedResponseDto<Page<Quotation>>> fetchAllQuotations(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "300", required = false) int pageSize) {

        Page<Quotation> quotations = quotationService.findAll(pageNo, pageSize);
        return PagedResponseDto.wrapSuccessResult(quotations, FETCH_SUCCESSFUL);
    }

    @Operation(summary = "Assign quotations to request items", tags = "QUOTATION")
    @PutMapping(value = "/quotations/assignToRequestItems")
    @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
    public ResponseEntity<ResponseDto<List<RequestItem>>> assignQuotationsToRequestItems(
            @RequestBody MapQuotationsToRequestItemsDto mappingDto) {

        Set<RequestItem> requestItems =
                mappingDto.getRequestItems().stream()
                        .filter(i -> requestItemService.existById(i.getId()))
                        .map(r -> requestItemService.findById(r.getId()))
                        .collect(Collectors.toSet());

        Set<Quotation> quotations =
                mappingDto.getQuotations().stream()
                        .filter(q -> quotationService.existByQuotationId(q.getId()))
                        .map(p -> quotationService.findById(p.getId()))
                        .collect(Collectors.toSet());

        List<RequestItem> result = quotationService.assignToRequestItem(requestItems, quotations);
        return ResponseDto.wrapSuccessResult(result, "QUOTATION ASSIGNMENT SUCCESSFUL");
    }

    @Operation(summary = "Assign document to quotation", tags = "QUOTATION")
    @PutMapping(value = "/quotations/{quotationId}/assignRequestDocument/{requestDocumentId}")
    @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
    public ResponseEntity<ResponseDto<Quotation>> assignRequestDocumentToQuotation(
            @PathVariable("quotationId") int quotationId,
            @PathVariable("requestDocumentId") int requestDocumentId) {

        RequestDocument requestDocument = documentService.findById(requestDocumentId);
        Quotation result =
                quotationService.assignRequestDocumentToQuotation(quotationId, requestDocument);
        return ResponseDto.wrapSuccessResult(result, "ASSIGN DOCUMENT SUCCESSFUL");
    }

    @GetMapping(value = "/quotations/supplierRequest")
    public ResponseEntity<ResponseDto<List<SupplierRequest>>> findSupplierWithNoDocAttachedToUnProcessedRequestItems(
            @RequestParam("registered") Optional<Boolean> registered) {

        if (registered.isPresent() && registered.get()) {
            List<Supplier> registeredSuppliers = supplierService.findSupplierWithNoDocAttachedToUnProcessedRequestItems();
            List<SupplierRequest> supplierRequests = getRequestSupplierPair(registeredSuppliers);
            return ResponseDto.wrapSuccessResult(supplierRequests, FETCH_SUCCESSFUL);
        }
        List<Supplier> unRegisteredSuppliers = supplierService.findUnRegisteredSupplierWithNoDocFromSRM();
        List<SupplierRequest> supplierRequests = getRequestSupplierPair(unRegisteredSuppliers);
        return ResponseDto.wrapSuccessResult(supplierRequests, FETCH_SUCCESSFUL);
    }

    @Operation(summary = "Get the request items whose quotations are without documents attached", tags = "QUOTATION")
    @GetMapping(value = "/requestItems/quotations")
    public ResponseEntity<ResponseDto<List<RequestItem>>> findRequestItemsWithoutDocsInQuotation(
            @RequestParam("withoutDocs") Boolean withoutDocs) {

        List<RequestItem> items = new ArrayList<>();
        if (!withoutDocs) {
            return ResponseDto.wrapSuccessResult(items, FETCH_SUCCESSFUL);
        }
        items = requestItemService.findRequestItemsWithoutDocInQuotation();
        return ResponseDto.wrapSuccessResult(items, "FETCH QUOTATIONS WITHOUT DOCUMENTS SUCCESSFUL");
    }

    @Operation(summary = "Generate quotation for unregistered suppliers", tags = "QUOTATION")
    @PostMapping(value = "/quotations/generateQuoteForSupplier")
    public ResponseEntity<ResponseDto<RequestDocument>> generateQuoteForSupplier(
            @RequestBody GeneratedQuoteDto generatedQuoteDto) throws FileNotFoundException {

        File file = generatedQuoteService.createQuoteForUnregisteredSupplier(generatedQuoteDto);

        String fileName = generateUniqueFileName(generatedQuoteDto);
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        RequestDocument requestDocument = documentService.storePdfFile(inputStream, fileName);
        return ResponseDto.wrapSuccessResult(requestDocument, "GENERATED QUOTATION");
    }

    @Operation(summary = "Approve quotations by auditor", tags = "QUOTATION")
    @PutMapping(value = "/quotations/approvals")
    public ResponseEntity<ResponseDto<List<QuotationMinorDto>>> approveBatchOfQuotations(
            Authentication authentication,
            @RequestBody @Valid @Size(min = 1, message = "Quotation Id is empty") Set<Integer> quotationIds
    ) {

        Employee auditor = employeeService.findEmployeeByEmail(authentication.getName());
        List<QuotationMinorDto> approvedQuotations = quotationService.approveByAuditor(quotationIds, auditor);
        return ResponseDto.wrapSuccessResult(approvedQuotations, FETCH_SUCCESSFUL);
    }

    private String generateUniqueFileName(GeneratedQuoteDto generatedQuoteDto) {
        String epoch = String.valueOf(System.currentTimeMillis());
        return "supplier_%s_%s.pdf".formatted(generatedQuoteDto.getSupplier().getId(), epoch);
    }

    private List<SupplierRequest> getRequestSupplierPair(List<Supplier> registeredSuppliers) {
        List<SupplierRequest> supplierRequests = new ArrayList<>();
        for (Supplier s : registeredSuppliers) {
            Set<RequestItem> requestItems =
                    requestItemService.findRequestItemsWithNoDocumentAttachedForSupplier(s.getId());

            if (!requestItems.isEmpty()) {
                SupplierRequest supplierRequest = new SupplierRequest();
                supplierRequest.setRequests(requestItems);
                supplierRequest.setSupplierName(s.getName());
                supplierRequest.setSupplierId(s.getId());
                supplierRequests.add(supplierRequest);
            }
        }
        return supplierRequests;
    }
}
