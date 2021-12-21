package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkFloatsDTO;
import com.logistics.supply.dto.ReceiveGoodsDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.event.listener.AddGRNListener;
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
import java.util.stream.Collectors;

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
      Set<GoodsReceivedNote> grnWithComment = getGRNWithComment(grnList);
      ResponseDTO response =
          new ResponseDTO("FETCH_GRN_WITH_INCOMPLETE_PAYMENT", SUCCESS, grnWithComment);
      return ResponseEntity.ok(response);
    }
    if (notApprovedByGM && checkAuthorityExist(authentication, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      List<GoodsReceivedNote> notes =
          goodsReceivedNoteService.findNonApprovedGRN(RequestReview.GM_REVIEW);
      Set<GoodsReceivedNote> notesWithComment = getGRNWithComment(notes);
      ResponseDTO response =
          new ResponseDTO("FETCH_GRN_WITHOUT_GM_APPROVAL", SUCCESS, notesWithComment);
      return ResponseEntity.ok(response);
    }
    if (notApprovedByHOD && checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      Department department =
          employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
      List<GoodsReceivedNote> notes =
          goodsReceivedNoteService.findGRNWithoutHodApprovalPerDepartment(department);
      Set<GoodsReceivedNote> noteWithComment = getGRNWithComment(notes);
      ResponseDTO response =
          new ResponseDTO("FETCH_GRN_WITHOUT_HOD_APPROVAL", SUCCESS, noteWithComment);
      return ResponseEntity.ok(response);
    }
    if (needPaymentAdvice
        && checkAuthorityExist(authentication, EmployeeRole.ROLE_PROCUREMENT_MANAGER)) {
      List<GoodsReceivedNote> goodsReceivedNotes =
          goodsReceivedNoteService.findGRNRequiringPaymentDate();
      Set<GoodsReceivedNote> grnWithComment = getGRNWithComment(goodsReceivedNotes);
      ResponseDTO response =
          new ResponseDTO("FETCH_GRN_REQUIRING_PAYMENT_ADVICE", SUCCESS, grnWithComment);
      return ResponseEntity.ok(response);
    }
    if(floatGrn && checkAuthorityExist(authentication, EmployeeRole.ROLE_HOD)) {
      Department  department = employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
      List<FloatGRN> floatGrnList = floatGRNService.getAllUnApprovedFloatGRN(department);
      ResponseDTO response =
              new ResponseDTO("FETCH_FLOAT_GRN_PENDING_HOD_APPROVAL_SUCCESSFUL", SUCCESS, floatGrnList);
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

  private Set<GoodsReceivedNote> getGRNWithComment(List<GoodsReceivedNote> notes) {
    return notes.stream()
        .map(
            i -> {
              List<GoodsReceivedNoteComment> noteForGM =
                  goodsReceivedNoteCommentService.findByGoodsReceivedNoteId(i.getId());
              i.setComments(noteForGM);
              return i;
            })
        .collect(Collectors.toSet());
  }

  @GetMapping(value = "/goodsReceivedNotes/suppliers/{supplierId}")
  public ResponseEntity<?> findGRNBySupplier(@PathVariable("supplierId") int supplierId) {
    List<GoodsReceivedNote> goodsReceivedNotes =
        goodsReceivedNoteService.findBySupplier(supplierId);
    if (!goodsReceivedNotes.isEmpty()) {
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, goodsReceivedNotes);
      return ResponseEntity.ok(response);
    }
    return notFound("GRN_NOT_FOUND");
  }

  @GetMapping(value = "/goodsReceivedNotes/{goodsReceivedNoteId}")
  public ResponseEntity<?> findGRNById(
      @PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId) {
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findGRNById(goodsReceivedNoteId);
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("FETCH_FAILED");
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, goodsReceivedNote);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/goodsReceivedNotes/invoices/{invoiceNo}")
  public ResponseEntity<?> findByInvoice(@PathVariable("invoiceNo") String invoiceNo) {
    Invoice i = invoiceService.findByInvoiceNo(invoiceNo);
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findByInvoice(i.getId());
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("INVOICE_NOT_FOUND");
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, goodsReceivedNote);
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
      long count = goodsReceivedNoteService.count();
      String ref = IdentifierUtil.idHandler("GRN", "STORES", String.valueOf(count));
      grn.setGrnRef(ref);
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

  @Operation(summary = "Generate the pdf format of the GRN using invoice id")
  @GetMapping(value = "/goodsReceivedNotes/{invoiceId}/download")
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

  @PostMapping("/goodsReceivedNotes/floats")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> receiveFloatItems(
      BulkFloatsDTO bulkFloats, Authentication authentication) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    Set<Floats> floats =
        bulkFloats.getFloats().stream()
            .filter(
                f ->
                    f.getStatus().equals(RequestStatus.PROCESSED)
                        && f.isFundsReceived() == Boolean.TRUE)
            .collect(Collectors.toSet());
    FloatGRN floatGRN = new FloatGRN();
    floatGRN.setFloats(floats);
    floatGRN.setCreatedBy(employee);
    FloatGRN saved = floatGRNService.save(floatGRN);
    if (saved == null) return failedResponse("REQUEST_FAILED");
    ResponseDTO response = new ResponseDTO("GRN_ISSUED_FOR_FLOAT_ITEMS", SUCCESS, saved);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Approve float GRN")
  @PutMapping("/goodsReceivedNotes/floats/{floatGrnId}")
  @PreAuthorize("hasRole('ROLE_HOD')")
  public ResponseEntity<?> approveFloatGRN(@PathVariable("floatGrnId") long floatGrnId) {
    try {
      FloatGRN floatGRN = floatGRNService.approveByHod(floatGrnId);
      if (floatGRN == null) return failedResponse("FLOAT_GRN_DOES_NOT_EXIST");
      ResponseDTO response = new ResponseDTO("FLOAT_GRN_APPROVED", SUCCESS, floatGRN);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("HOD_APPROVE_FLOAT_GRN_FAILED");
  }

  @GetMapping("/goodsReceivedNotes/floats/{floatGrnId}")
  public ResponseEntity<?> getFloatGRN(@PathVariable("floatGRNId") int floatGRNId) {
    FloatGRN goodsReceivedNote = floatGRNService.findById(floatGRNId);
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("FETCH_FAILED");
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, goodsReceivedNote);
    return ResponseEntity.ok(response);
  }

  private Boolean checkAuthorityExist(Authentication authentication, EmployeeRole role) {
    return authentication.getAuthorities().stream()
        .map(x -> x.getAuthority().equalsIgnoreCase(role.name()))
        .filter(x -> x == true)
        .findAny()
        .get();
  }

//  private boolean checkForFloatsFoeDepartment(Set<FloatGRN> floatGRNS, Department department) {
////    floatGRNS.stream().flatMap(c-> c.get).stream().collect(Collectors.groupingBy())
//  }


}
