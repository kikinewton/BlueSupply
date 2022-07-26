package com.logistics.supply.controller;

import com.logistics.supply.dto.ReceiveGoodsDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.listener.GRNListener;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.IdentifierUtil;
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

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;
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
  @Autowired GoodsReceivedNoteCommentService goodsReceivedNoteCommentService;
  @Autowired private FloatGRNService floatGRNService;

  @GetMapping(value = "/goodsReceivedNotes")
  public ResponseEntity<?> findAllGRN(
      Authentication authentication,
      @RequestParam(defaultValue = "false", required = false) Boolean paymentInComplete,
      @RequestParam(defaultValue = "false", required = false) Boolean notApprovedByHOD,
      @RequestParam(defaultValue = "false", required = false) Boolean notApprovedByGM,
      @RequestParam(defaultValue = "false", required = false) Boolean needPaymentAdvice,
      @RequestParam(defaultValue = "false", required = false) Boolean floatGrn,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) {
    if (paymentInComplete) {
      List<GoodsReceivedNote> grnList = goodsReceivedNoteService.findGRNWithoutCompletePayment();
      if (grnList.isEmpty()) return notFound("NO GRN FOUND");

      ResponseDTO response = new ResponseDTO("FETCH GRN WITH INCOMPLETE PAYMENT", SUCCESS, grnList);
      return ResponseEntity.ok(response);
    }
    if (notApprovedByGM && checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      List<GoodsReceivedNote> notes =
          goodsReceivedNoteService.findNonApprovedGRN(RequestReview.GM_REVIEW);

      return ResponseDTO.wrapSuccessResult(notes, "FETCH GRN WITHOUT GM APPROVAL");
    }
    if (notApprovedByHOD && checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      Department department =
          employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
      List<GoodsReceivedNote> notes =
          goodsReceivedNoteService.findGRNWithoutHodApprovalPerDepartment(department);
      return ResponseDTO.wrapSuccessResult(notes, FETCH_SUCCESSFUL);
    }
    if (needPaymentAdvice
        && checkAuthorityExist(authentication, EmployeeRole.ROLE_PROCUREMENT_MANAGER)) {
      List<GoodsReceivedNote> goodsReceivedNotes =
          goodsReceivedNoteService.findGRNRequiringPaymentDate();
      return ResponseDTO.wrapSuccessResult(goodsReceivedNotes, FETCH_SUCCESSFUL);
    }
    if (floatGrn && checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      Department department =
          employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
      List<FloatGRN> floatGrnList = floatGRNService.getAllUnApprovedFloatGRN(department);
      return ResponseDTO.wrapSuccessResult(
          floatGrnList, "FETCH FLOAT GRN PENDING HOD APPROVAL SUCCESSFUL");
    }
    List<GoodsReceivedNote> goodsReceivedNotes = new ArrayList<>();
    try {
      goodsReceivedNotes.addAll(goodsReceivedNoteService.findAllGRN(pageNo, pageSize));
      return ResponseDTO.wrapSuccessResult(goodsReceivedNotes, FETCH_SUCCESSFUL );
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("GRN NOT FOUND");
  }

  @GetMapping(value = "/goodsReceivedNotes/suppliers/{supplierId}")
  public ResponseEntity<?> findGRNBySupplier(@PathVariable("supplierId") int supplierId) {
    List<GoodsReceivedNote> goodsReceivedNotes =
        goodsReceivedNoteService.findBySupplier(supplierId);
    if (!goodsReceivedNotes.isEmpty()) {
      ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, goodsReceivedNotes);
      return ResponseEntity.ok(response);
    }
    return notFound("GRN NOT FOUND");
  }

  @GetMapping(value = "/goodsReceivedNotes/{goodsReceivedNoteId}")
  public ResponseEntity<?> findGRNById(@PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId)
      throws GeneralException {
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findGRNById(goodsReceivedNoteId);
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("FETCH FAILED");
    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, goodsReceivedNote);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/goodsReceivedNotes/invoices/{invoiceNo}")
  public ResponseEntity<?> findByInvoice(@PathVariable("invoiceNo") String invoiceNo) {
    Invoice i = invoiceService.findByInvoiceNo(invoiceNo);
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findByInvoice(i.getId());
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("INVOICE NOT FOUND");
    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, goodsReceivedNote);
    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/goodsReceivedNotes")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> receiveRequestItems(
      @Valid @RequestBody ReceiveGoodsDTO receiveGoods, Authentication authentication) {
    try {
      boolean docExist =
          requestDocumentService.verifyIfDocExist(
              receiveGoods.getInvoice().getInvoiceDocument().getId());
      if (!docExist) return failedResponse("INVOICE DOCUMENT DOES NOT EXIST");
      Invoice inv = new Invoice();
      BeanUtils.copyProperties(receiveGoods.getInvoice(), inv);
      Invoice i = invoiceService.saveInvoice(inv);
      if (Objects.isNull(i)) return failedResponse("INVOICE DOES NOT EXIST");

      GoodsReceivedNote grn = new GoodsReceivedNote();
      LocalPurchaseOrder lpoExist =
          localPurchaseOrderService.findLpoById(receiveGoods.getLocalPurchaseOrder().getId());
      if (Objects.isNull(lpoExist)) return failedResponse("LPO DOES NOT EXIST");
      grn.setSupplier(i.getSupplier().getId());
      grn.setInvoice(i);
      grn.setReceivedItems(receiveGoods.getRequestItems());
      grn.setLocalPurchaseOrder(lpoExist);
      long count = goodsReceivedNoteService.count();
      String ref = IdentifierUtil.idHandler("GRN", "STORES", String.valueOf(count));
      grn.setGrnRef(ref);
      grn.setInvoiceAmountPayable(receiveGoods.getInvoiceAmountPayable());
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      grn.setCreatedBy(employee);
      GoodsReceivedNote savedGrn = goodsReceivedNoteService.saveGRN(grn);
      if (Objects.nonNull(savedGrn)) {
        GRNListener.GRNEvent grnEvent = new GRNListener.GRNEvent(this, savedGrn);
        applicationEventPublisher.publishEvent(grnEvent);
        ResponseDTO response = new ResponseDTO("GRN CREATED", SUCCESS, savedGrn);
        return ResponseEntity.ok(response);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("CREATE GRN FAILED");
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
      return ResponseDTO.wrapSuccessResult(grn,"GRN APPROVED");
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("APPROVE GRN FAILED");
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
        if (Objects.isNull(grn)) return failedResponse("GRN INVALID");
        ResponseDTO response = new ResponseDTO("GRN APPROVED", SUCCESS, grn);
        return ResponseEntity.ok(response);
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
          ResponseDTO response = new ResponseDTO("UPDATED GRN", SUCCESS, updated.get());
          return ResponseEntity.ok(response);
        }
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("UPDATE GRN FAILED");
  }

  //  @PostMapping("/goodsReceivedNotes/floats")
  //  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  //  public ResponseEntity<?> receiveFloatItems(
  //      BulkFloatsDTO bulkFloats, Authentication authentication) {
  //    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
  //    FloatGRN saved = floatGRNService.issueFloatGRN(bulkFloats.getFloats(), employee);
  //    if (saved == null) return failedResponse("REQUEST_FAILED");
  //    ResponseDTO response = new ResponseDTO("GRN_ISSUED_FOR_FLOAT_ITEMS", SUCCESS, saved);
  //    return ResponseEntity.ok(response);
  //  }

  @Operation(summary = "Approve float GRN")
  @PutMapping("/goodsReceivedNotes/floats/{floatGrnId}")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> approveFloatGRN(@PathVariable("floatGrnId") long floatGrnId) {
    try {
      FloatGRN floatGRN = floatGRNService.approveByHod(floatGrnId);
      if (floatGRN == null) return failedResponse("FLOAT GRN DOES NOT EXIST");
      ResponseDTO response = new ResponseDTO("FLOAT GRN APPROVED", SUCCESS, floatGRN);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("HOD APPROVE FLOAT GRN FAILED");
  }

  @GetMapping("/goodsReceivedNotes/floats/{floatGrnId}")
  public ResponseEntity<?> getFloatGRN(@PathVariable("floatGRNId") int floatGRNId) {
    FloatGRN goodsReceivedNote = floatGRNService.findById(floatGRNId);
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("FETCH FAILED");
    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, goodsReceivedNote);
    return ResponseEntity.ok(response);
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x == true)
        .findAny()
        .get();
  }
}
