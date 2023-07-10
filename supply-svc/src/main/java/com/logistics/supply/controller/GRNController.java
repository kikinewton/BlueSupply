package com.logistics.supply.controller;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.listener.GRNListener;
import com.logistics.supply.interfaces.projections.GRNView;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import com.logistics.supply.util.IdentifierUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class GRNController {

  private final ApplicationEventPublisher applicationEventPublisher;
  private final LocalPurchaseOrderService localPurchaseOrderService;
  private final InvoiceService invoiceService;
  private final GoodsReceivedNoteService goodsReceivedNoteService;
  private final RequestDocumentService requestDocumentService;
  private final RoleService roleService;
  private final EmployeeService employeeService;
  private final FloatGRNService floatGRNService;

  @GetMapping(value = "/goodsReceivedNotes")
  public ResponseEntity<?> findAllGRN(
      Authentication authentication,
      @RequestParam(defaultValue = "false", required = false) Boolean paymentInComplete,
      @RequestParam(defaultValue = "false", required = false) Boolean notApprovedByHOD,
      @RequestParam(defaultValue = "false", required = false) Boolean notApprovedByGM,
      @RequestParam(defaultValue = "false", required = false) Boolean needPaymentAdvice,
      @RequestParam(defaultValue = "false", required = false) Boolean floatGrn,
      @RequestParam(defaultValue = "false", required = false) Optional<Boolean> allFloatGrn,
      @RequestParam(defaultValue = "false", required = false) Optional<Boolean> overDueGrn,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) {

    if (paymentInComplete) {
      List<GoodsReceivedNote> grnList = goodsReceivedNoteService.findGRNWithoutCompletePayment();
      return ResponseDto.wrapSuccessResult(grnList, Constants.FETCH_SUCCESSFUL);
    }
    if (notApprovedByGM && checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      List<GoodsReceivedNote> notes =
          goodsReceivedNoteService.findNonApprovedGRN(RequestReview.GM_REVIEW);
      return ResponseDto.wrapSuccessResult(notes, Constants.FETCH_SUCCESSFUL);
    }
    Employee employeeByEmail = employeeService.findEmployeeByEmail(authentication.getName());
    Department department = employeeByEmail.getDepartment();
    if (notApprovedByHOD && checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      List<GoodsReceivedNote> notes =
          goodsReceivedNoteService.findGRNWithoutHodApprovalPerDepartment(department);
      return ResponseDto.wrapSuccessResult(notes, Constants.FETCH_SUCCESSFUL);
    }
    if (needPaymentAdvice
        && checkAuthorityExist(authentication, EmployeeRole.ROLE_PROCUREMENT_MANAGER)) {
      List<GoodsReceivedNote> goodsReceivedNotes =
          goodsReceivedNoteService.findGRNRequiringPaymentDate();
      return ResponseDto.wrapSuccessResult(goodsReceivedNotes, Constants.FETCH_SUCCESSFUL);
    }
    if (overDueGrn.isPresent()
        && overDueGrn.get()
        && checkAuthorityExist(authentication, EmployeeRole.ROLE_PROCUREMENT_MANAGER)) {
      Pageable pageable = PageRequest.of(pageNo, pageSize);
      Page<GRNView> grnWithPaymentDateExceeded =
          goodsReceivedNoteService.findGrnWithPaymentDateExceeded(pageable);
      return PagedResponseDto.wrapSuccessResult(grnWithPaymentDateExceeded, Constants.FETCH_SUCCESSFUL);
    }
    if (floatGrn && checkAuthorityExist(authentication, EmployeeRole.ROLE_AUDITOR)) {
      List<FloatGRN> floatGrnList = floatGRNService.getAllApprovedFloatGRNForAuditor();
      return ResponseDto.wrapSuccessResult(floatGrnList, Constants.FETCH_SUCCESSFUL);
    }
    if (floatGrn && checkAuthorityExist(authentication, EmployeeRole.ROLE_STORE_MANAGER)) {
      List<FloatGrnDto> floatGrnPendingApproval =
          floatGRNService.findFloatGrnPendingApproval(department.getId());
      return ResponseDto.wrapSuccessResult(floatGrnPendingApproval, Constants.FETCH_SUCCESSFUL);
    }
    if (allFloatGrn.isPresent() && allFloatGrn.get()) {
      Pageable pageable = PageRequest.of(pageNo, pageSize);
      Page<FloatGrnDto> allFloatGrn1 = floatGRNService.findAllFloatGrn(department.getId(), pageable);
      return PagedResponseDto.wrapSuccessResult(allFloatGrn1, Constants.FETCH_SUCCESSFUL);
    }

    Page<GoodsReceivedNote> allGRN = goodsReceivedNoteService.findAllGRN(pageNo, pageSize);
    return PagedResponseDto.wrapSuccessResult(allGRN, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/goodsReceivedNotes/suppliers/{supplierId}")
  public ResponseEntity<?> findGRNBySupplier(@PathVariable("supplierId") int supplierId) {
    List<GoodsReceivedNote> goodsReceivedNotes =
        goodsReceivedNoteService.findBySupplier(supplierId);
    if (!goodsReceivedNotes.isEmpty()) {
      ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", Constants.SUCCESS, goodsReceivedNotes);
      return ResponseEntity.ok(response);
    }
    return Helper.notFound("GRN NOT FOUND");
  }

  @GetMapping(value = "/goodsReceivedNotes/{goodsReceivedNoteId}")
  public ResponseEntity<?> findGRNById(@PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId) {

    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findGRNById(goodsReceivedNoteId);
    return ResponseDto.wrapSuccessResult(goodsReceivedNote, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/goodsReceivedNotes/invoices/{invoiceNo}")
  public ResponseEntity<?> findByInvoice(@PathVariable("invoiceNo") String invoiceNo) {
    Invoice i = invoiceService.findByInvoiceNo(invoiceNo);
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findByInvoice(i.getId());
    if (Objects.isNull(goodsReceivedNote)) return Helper.failedResponse("INVOICE NOT FOUND");
    ResponseDto response = new ResponseDto("FETCH SUCCESSFUL", Constants.SUCCESS, goodsReceivedNote);
    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/goodsReceivedNotes")
//  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> receiveRequestItems(
          @Valid @RequestBody ReceiveGoodsDto receiveGoods,
          Authentication authentication) {

          requestDocumentService.verifyIfDocExist(
              receiveGoods.getInvoice().getInvoiceDocument().getId());
      InvoiceDto invoice = receiveGoods.getInvoice();
      Invoice savedInvoice = invoiceService.saveInvoice(invoice);

      GoodsReceivedNote grn = new GoodsReceivedNote();
      LocalPurchaseOrder localPurchaseOrder =
          localPurchaseOrderService.findLpoById(receiveGoods.getLocalPurchaseOrder().getId());

      grn.setSupplier(savedInvoice.getSupplier().getId());
      grn.setInvoice(savedInvoice);
      grn.setReceivedItems(receiveGoods.getRequestItems());
      grn.setLocalPurchaseOrder(localPurchaseOrder);
      long count = goodsReceivedNoteService.count();
      String ref = IdentifierUtil.idHandler("GRN", "STORES", String.valueOf(count));
      grn.setGrnRef(ref);
      grn.setInvoiceAmountPayable(receiveGoods.getInvoiceAmountPayable());
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      grn.setCreatedBy(employee);
      GoodsReceivedNote savedGrn = goodsReceivedNoteService.saveGRN(grn);
      CompletableFuture.runAsync(() -> {

          GRNListener.GRNEvent grnEvent = new GRNListener.GRNEvent(this, savedGrn);
          applicationEventPublisher.publishEvent(grnEvent);
      });
      return ResponseDto.wrapSuccessResult(savedGrn, "GRN CREATED");


  }

  @Operation(summary = "Approve the GRN issued by stores")
  @PutMapping("/goodsReceivedNotes/{grnId}/approve")
  @PreAuthorize("hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER')")
  public ResponseEntity<?> approveGRN(
      Authentication authentication, @PathVariable("grnId") int grnId) {
    try {
      int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
      EmployeeRole employeeRole = roleService.getEmployeeRole(authentication);
      GoodsReceivedNote grn = goodsReceivedNoteService.approveGRN(grnId, employeeId, employeeRole);
      return ResponseDto.wrapSuccessResult(grn, "GRN APPROVED");
    } catch (Exception e) {
      log.error(e.toString());
    }
    return Helper.failedResponse("APPROVE GRN FAILED");
  }

  @Operation(summary = "Generate the pdf format of the GRN using invoice id")
  @GetMapping(value = "/goodsReceivedNotes/{invoiceId}/download")
  public void generatePdfGRN(
      @PathVariable("invoiceId") int invoiceId, HttpServletResponse response) {
    try {

      Invoice i = invoiceService.findByInvoiceId(invoiceId);

      if (Objects.isNull(i)) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
      File file = goodsReceivedNoteService.generatePdfOfGRN(i.getId());
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

  @PutMapping(value = "/goodsReceivedNotes/{goodsReceivedNoteId}")
  @PreAuthorize(
      "hasRole('ROLE_HOD') or hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> updateGRN(
      Authentication authentication,
      @PathVariable("goodsReceivedNoteId") long goodsReceivedNoteId,
      @RequestParam(defaultValue = "false", required = false) Boolean approveGRN,
      @RequestParam(defaultValue = "false", required = false) Boolean paymentAdvice,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          Date paymentDate) {
    try {
      if (approveGRN
          && (checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)
              || checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER))) {
        int employeeId = employeeService.findEmployeeByEmail(authentication.getName()).getId();
        EmployeeRole employeeRole = roleService.getEmployeeRole(authentication);

        GoodsReceivedNote grn =
            goodsReceivedNoteService.approveGRN(goodsReceivedNoteId, employeeId, employeeRole);
        return ResponseDto.wrapSuccessResult(grn, "GRN APPROVED");
      }
      if (paymentAdvice
          && paymentDate.after(new Date())
          && checkAuthorityExist(authentication, EmployeeRole.ROLE_PROCUREMENT_MANAGER)) {
        Optional<GoodsReceivedNote> updated =
            Optional.ofNullable(goodsReceivedNoteService.findGRNById(goodsReceivedNoteId))
                .filter(r -> r.isApprovedByHod())
                .map(
                    x -> {
                      x.setPaymentDate(paymentDate);
                      int employeeId =
                          employeeService.findEmployeeByEmail(authentication.getName()).getId();
                      x.setProcurementManagerId(employeeId);
                      return goodsReceivedNoteService.saveGRN(x);
                    });
        if (updated.isPresent()) {
          ResponseDto response = new ResponseDto("UPDATED GRN", Constants.SUCCESS, updated.get());
          return ResponseEntity.ok(response);
        }
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return Helper.failedResponse("UPDATE GRN FAILED");
  }

  @PostMapping("/goodsReceivedNotes/floats")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<ResponseDto<FloatGrnDto>> receiveFloatItems(
          @RequestBody BulkFloatsDTO bulkFloats, Authentication authentication)
      throws GeneralException {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    FloatGrnDto saved = floatGRNService.issueFloatGRN(bulkFloats, employee);
    return ResponseDto.wrapSuccessResult(saved, "GRN ISSUED FOR FLOAT");
  }

  @Operation(summary = "Approve float GRN")
  @PutMapping("/goodsReceivedNotes/floats/{floatGrnId}")
  @PreAuthorize("hasRole('ROLE_STORE_MANAGER')")
  public ResponseEntity<ResponseDto<FloatGrnDto>> approveFloatGRN(
      @PathVariable("floatGrnId") long floatGrnId, Authentication authentication)
      throws GeneralException {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    FloatGrnDto floatGRN = floatGRNService.approveByStoreManager(floatGrnId, employee.getId());
    return ResponseDto.wrapSuccessResult(floatGRN, "FLOAT GRN APPROVED");
  }

  @GetMapping("/goodsReceivedNotes/floats/{floatGrnId}")
  public ResponseEntity<ResponseDto<FloatGrnDto>> getFloatGRN(
      @PathVariable("floatGrnId") int floatGrnId) throws GeneralException {
    FloatGRN goodsReceivedNote = floatGRNService.findById(floatGrnId);
    FloatGrnDto floatGrnDTO = FloatGrnDto.toDto(goodsReceivedNote);
    return ResponseDto.wrapSuccessResult(floatGrnDTO, Constants.FETCH_SUCCESSFUL);
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x)
        .findAny()
        .orElse(false);
  }
}
