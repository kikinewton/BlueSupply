package com.logistics.supply.controller;

import com.logistics.supply.dto.GoodsReceivedNoteDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class GRNController {

  @Autowired
  ApplicationEventPublisher applicationEventPublisher;
  @Autowired LocalPurchaseOrderService localPurchaseOrderService;
  @Autowired InvoiceService invoiceService;
  @Autowired GoodsReceivedNoteService goodsReceivedNoteService;
  @Autowired RequestDocumentService requestDocumentService;
  @Autowired RoleService roleService;
  @Autowired EmployeeService employeeService;

  @PostMapping(value = "/goodsReceivedNote")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> addGRN(@Valid @RequestBody GoodsReceivedNoteDTO goodsReceivedNote) {
    GoodsReceivedNote grn = new GoodsReceivedNote();
    LocalPurchaseOrder lpoExist =
        localPurchaseOrderService.findLpoById(goodsReceivedNote.getLpo().getId());
    if (lpoExist == null) return failedResponse("LPO_NOT_FOUND");
    Invoice invoice = invoiceService.findByInvoiceId(goodsReceivedNote.getInvoice().getId());
    if (invoice == null) return failedResponse("INVOICE_NOT_FOUND");
    if (Objects.nonNull(lpoExist) && Objects.nonNull(invoice)) {

      BeanUtils.copyProperties(goodsReceivedNote, grn);
      grn.setInvoice(invoice);
      grn.setLocalPurchaseOrder(lpoExist);
      GoodsReceivedNote savedGrn = goodsReceivedNoteService.saveGRN(grn);
      if (Objects.nonNull(savedGrn)) {
        ResponseDTO response = new ResponseDTO("GRN_ADDED_SUCCESSFULLY", SUCCESS, savedGrn);
        return ResponseEntity.ok(response);
      }
      return failedResponse("SAVE_GRN_FAILED");
    }
    BeanUtils.copyProperties(goodsReceivedNote, grn);
    grn.setInvoice(invoice);
    grn.setLocalPurchaseOrder(lpoExist);
    GoodsReceivedNote savedGrn = goodsReceivedNoteService.saveGRN(grn);
    if (Objects.isNull(grn)) return failedResponse("SAVE_FAILED");
    ResponseDTO response = new ResponseDTO("SAVE_SUCCESSFUL", SUCCESS, savedGrn);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/goodsReceivedNote")
  public ResponseEntity<?> findAllGRN(
      Authentication authentication,
      @RequestParam(defaultValue = "false", required = false) Boolean paymentInComplete,
      @RequestParam(defaultValue = "false", required = false) Boolean notApprovedByHOD,
      @RequestParam(defaultValue = "false", required = false) Boolean notApprovedByGM,
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

  @PutMapping(value = "/goodsReceivedNote/{goodsReceivedNoteId}")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> updateGRN(
      @PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId,
      @Valid @RequestBody GoodsReceivedNoteDTO goodsReceivedNoteDTO) {
    GoodsReceivedNote grn = goodsReceivedNoteService.findGRNById(goodsReceivedNoteId);
    if (Objects.isNull(grn)) return failedResponse("GRN_DOES_NOT_EXIST");
    LocalPurchaseOrder lpo =
        localPurchaseOrderService.findLpoById(goodsReceivedNoteDTO.getLpo().getId());
    if (Objects.isNull(lpo)) return failedResponse("LPO_DOES_NOT_EXIST");
    Invoice invoice = invoiceService.findByInvoiceId(goodsReceivedNoteDTO.getInvoice().getId());
    if (Objects.isNull(invoice)) return failedResponse("INVOICE_DOES_NOT_EXIST");
    GoodsReceivedNote updatedGrn =
        goodsReceivedNoteService.updateGRN(goodsReceivedNoteId, goodsReceivedNoteDTO);
    if (Objects.isNull(updatedGrn)) return failedResponse("UPDATE_GRN_FAILED");
    ResponseDTO response = new ResponseDTO("UPDATE_GRN_SUCCESSFUL", SUCCESS, updatedGrn);
    return ResponseEntity.ok(response);
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

  @PostMapping(value = "/receiveGoods")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> receiveRequestItems(@Valid @RequestBody ReceiveGoodsDTO receiveGoods, Authentication authentication) {
    try {
      System.out.println("receiveGoods = " + receiveGoods.getInvoice());
      boolean docExist =
          requestDocumentService.verifyIfDocExist(
              receiveGoods.getInvoice().getInvoiceDocument().getId());
      if (!docExist) return failedResponse("INVOICE_DOCUMENT_DOES_NOT_EXIST");
      Invoice inv = new Invoice();
      BeanUtils.copyProperties(receiveGoods.getInvoice(), inv);
      Invoice i = invoiceService.saveInvoice(inv);
      if (Objects.isNull(i)) return failedResponse("INVOICE_DOES_NOT_EXIST");

      System.out.println("invoice created  = " + i);
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
        System.out.println("savedGrn saved = " + savedGrn);
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

  @GetMapping(value = "grn/{invoiceId}")
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

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x == true)
        .findAny()
        .get();
  }
}
