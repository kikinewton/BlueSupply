package com.logistics.supply.controller;

import com.logistics.supply.dto.ReceiveGoodsDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.event.listener.AddGRNListener;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class GRNController {

  @Autowired ApplicationEventPublisher applicationEventPublisher;
  @Autowired LocalPurchaseOrderService localPurchaseOrderService;
  @Autowired InvoiceService invoiceService;
  @Autowired GoodsReceivedNoteService goodsReceivedNoteService;
  @Autowired RequestDocumentService requestDocumentService;
  @Autowired RoleService roleService;
  @Autowired EmployeeService employeeService;

  @GetMapping(value = "/goodsReceivedNotes")
  public ResponseEntity<?> findAllGRN(
      Authentication authentication,
      @RequestParam(defaultValue = "false", required = false) Boolean paymentInComplete,
      @RequestParam(defaultValue = "false", required = false) Boolean notApprovedByHOD,
      @RequestParam(defaultValue = "false", required = false) Boolean notApprovedByGM,
      @RequestParam(defaultValue = "false", required = false) Boolean paymentAdvice,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "100") int pageSize) {
    if (paymentInComplete) {
      List<GoodsReceivedNote> grnList = goodsReceivedNoteService.findGRNWithoutCompletePayment();
      ResponseDTO response = new ResponseDTO("FETCH_GRN_WITH_INCOMPLETE_PAYMENT", SUCCESS, grnList);
      return ResponseEntity.ok(response);
    }
    if (notApprovedByGM && checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      List<GoodsReceivedNote> notes =
          goodsReceivedNoteService.findNonApprovedGRN(RequestReview.GM_REVIEW);
      ResponseDTO response = new ResponseDTO("FETCH_GRN_WITHOUT_GM_APPROVAL", SUCCESS, notes);
      return ResponseEntity.ok(response);
    }
    if (notApprovedByHOD && checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      Department department =
          employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
      List<GoodsReceivedNote> notes =
          goodsReceivedNoteService.findGRNWithoutHodApprovalPerDepartment(department);
      ResponseDTO response = new ResponseDTO("FETCH_GRN_WITHOUT_HOD_APPROVAL", SUCCESS, notes);
      return ResponseEntity.ok(response);
    }
    if (paymentAdvice
        && checkAuthorityExist(authentication, EmployeeRole.ROLE_PROCUREMENT_MANAGER)) {
      List<GoodsReceivedNote> goodsReceivedNotes =
          goodsReceivedNoteService.findGRNRequiringPaymentDate();
      ResponseDTO response =
          new ResponseDTO("FETCH_GRN_REQUIRING_PAYMENT_ADVICE", SUCCESS, goodsReceivedNotes);
      return ResponseEntity.ok(response);
    }
    List<GoodsReceivedNote> goodsReceivedNotes = new ArrayList<>();
    try {
      goodsReceivedNotes.addAll(goodsReceivedNoteService.findAllGRN(pageNo, pageSize));
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, goodsReceivedNotes);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("GRN_NOT_FOUND");
  }

  @GetMapping(value = "/goodsReceivedNote/suppliers/{supplierId}")
  public ResponseEntity<?> findGRNBySupplier(@PathVariable("supplierId") int supplierId) {
    List<GoodsReceivedNote> goodsReceivedNotes =
        goodsReceivedNoteService.findBySupplier(supplierId);
    if (!goodsReceivedNotes.isEmpty()) {
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, goodsReceivedNotes);
      return ResponseEntity.ok(response);
    }
    return notFound("GRN_NOT_FOUND");
  }

  @GetMapping(value = "/goodsReceivedNote/{goodsReceivedNoteId}")
  public ResponseEntity<?> findGRNById(
      @PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId) {
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findGRNById(goodsReceivedNoteId);
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("FETCH_FAILED");
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, goodsReceivedNote);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/goodsReceivedNote/invoices/{invoiceNo}")
  public ResponseEntity<?> findByInvoice(@PathVariable("invoiceNo") String invoiceNo) {
    Invoice i = invoiceService.findByInvoiceNo(invoiceNo);
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findByInvoice(i.getId());
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("INVOICE_NOT_FOUND");
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, goodsReceivedNote);
    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/goodsReceivedNote")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> receiveRequestItems(
      @Valid @RequestBody ReceiveGoodsDTO receiveGoods, Authentication authentication) {
    try {
      boolean docExist =
          requestDocumentService.verifyIfDocExist(
              receiveGoods.getInvoice().getInvoiceDocument().getId());
      if (!docExist) return failedResponse("INVOICE_DOCUMENT_DOES_NOT_EXIST");
      Invoice inv = new Invoice();
      BeanUtils.copyProperties(receiveGoods.getInvoice(), inv);
      Invoice i = invoiceService.saveInvoice(inv);
      if (Objects.isNull(i)) return failedResponse("INVOICE_DOES_NOT_EXIST");

      GoodsReceivedNote grn = new GoodsReceivedNote();
      LocalPurchaseOrder lpoExist =
          localPurchaseOrderService.findLpoById(receiveGoods.getLocalPurchaseOrder().getId());
      if (Objects.isNull(lpoExist)) return failedResponse("LPO_DOES_NOT_EXIST");
      grn.setSupplier(i.getSupplier().getId());
      grn.setInvoice(i);
      grn.setReceivedItems(receiveGoods.getRequestItems());
      grn.setLocalPurchaseOrder(lpoExist);
      grn.setInvoiceAmountPayable(receiveGoods.getInvoiceAmountPayable());
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      grn.setCreatedBy(employee);
      GoodsReceivedNote savedGrn = goodsReceivedNoteService.saveGRN(grn);
      if (Objects.nonNull(savedGrn)) {
        AddGRNListener.GRNEvent grnEvent = new AddGRNListener.GRNEvent(this, savedGrn);
        applicationEventPublisher.publishEvent(grnEvent);
        ResponseDTO response = new ResponseDTO("GRN_CREATED", SUCCESS, savedGrn);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("CREATE_GRN_FAILED");
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
      if (Objects.isNull(grn)) return failedResponse("GRN_INVALID");
      ResponseDTO response = new ResponseDTO("GRN_APPROVED", SUCCESS, grn);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("APPROVE_GRN_FAILED");
  }

  @GetMapping(value = "/goodsReceivedNote/{invoiceId}")
  public void generatePdfGRN(
      @PathVariable("invoiceId") int invoiceId, HttpServletResponse response) {
    try {

      Invoice i = invoiceService.findByInvoiceId(invoiceId);

      if (Objects.isNull(i)) return;
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
        if (Objects.isNull(grn)) return failedResponse("GRN_INVALID");
        ResponseDTO response = new ResponseDTO("GRN_APPROVED", SUCCESS, grn);
        return ResponseEntity.ok(response);
      }
      if (paymentAdvice
          && paymentDate.after(new Date())
          && checkAuthorityExist(authentication, EmployeeRole.ROLE_PROCUREMENT_MANAGER)) {
        Optional<GoodsReceivedNote> updated =
            Optional.ofNullable(goodsReceivedNoteService.findGRNById(goodsReceivedNoteId))
                .filter(r -> r.isApprovedByGm() && r.isApprovedByHod())
                .map(
                    x -> {
                      x.setPaymentDate(paymentDate);
                      return goodsReceivedNoteService.saveGRN(x);
                    });
        if (updated.isPresent()) {
          ResponseDTO response = new ResponseDTO("UPDATED_GRN", SUCCESS, updated.get());
          return ResponseEntity.ok(response);
        }
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("UPDATE_GRN_FAILED");
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x == true)
        .findAny()
        .get();
  }
}
