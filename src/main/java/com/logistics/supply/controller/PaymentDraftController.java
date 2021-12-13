package com.logistics.supply.controller;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class PaymentDraftController {

  @Autowired GoodsReceivedNoteService goodsReceivedNoteService;
  @Autowired PaymentDraftService paymentDraftService;
  @Autowired EmployeeService employeeService;
  @Autowired RoleService roleService;
  @Autowired PaymentService paymentService;

  @PostMapping(value = "/paymentDraft")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> savePaymentDraft(
      @Valid @RequestBody PaymentDraftDTO paymentDraftDTO, Authentication authentication) {
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteService.findGRNById(
            Objects.requireNonNull(
                paymentDraftDTO.getGoodsReceivedNote().getId(), "GRN_CAN_NOT_BE_NULL"));
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("GRN_IS_INVALID");
    PaymentDraft paymentDraft = new PaymentDraft();
    paymentDraft.setGoodsReceivedNote(goodsReceivedNote);
    BeanUtils.copyProperties(paymentDraftDTO, paymentDraft);
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    paymentDraft.setCreatedBy(employee);
    try {
      PaymentDraft saved = paymentDraftService.savePaymentDraft(paymentDraft);
      ResponseDTO response = new ResponseDTO("PAYMENT_DRAFT_ADDED", SUCCESS, saved);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("PAYMENT_DRAFT_FAILED");
  }

  @PutMapping(value = "/paymentDraft/{paymentDraftId}")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> updatePaymentDraft(
      @PathVariable("paymentDraftId") int paymentDraftId,
      @Valid @RequestBody PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    PaymentDraft draft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(draft)) return failedResponse("PAYMENT_DRAFT_DOES_NOT_EXIST");
    PaymentDraft paymentDraft =
        paymentDraftService.updatePaymentDraft(paymentDraftId, paymentDraftDTO);
    if (Objects.isNull(paymentDraft)) return failedResponse("UPDATE_PAYMENT_DRAFT_FAILED");
    ResponseDTO response =
        new ResponseDTO<>("UPDATE_PAYMENT_DRAFT_SUCCESSFUL", SUCCESS, paymentDraft);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/paymentDraft/{paymentDraftId}")
  public ResponseEntity<?> findDraftById(@PathVariable("paymentDraftId") int paymentDraftId) {
    PaymentDraft paymentDraft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(paymentDraft)) return failedResponse("PAYMENT_DRAFT_NOT_FOUND");
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, paymentDraft);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/paymentDrafts")
  @PreAuthorize(
      "hasRole('ROLE_AUDITOR') or hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_FINANCIAL_MANAGER')")
  public ResponseEntity<?> listPaymentDrafts(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      Authentication authentication) {
    List<PaymentDraft> drafts = new ArrayList<>();
    EmployeeRole employeeRole = roleService.getEmployeeRole(authentication);

    drafts.addAll(paymentDraftService.findAllDrafts(pageNo, pageSize, employeeRole));
    if (drafts.isEmpty()) return notFound("NO_PAYMENT_DRAFT_FOUND");
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, drafts);
    return ResponseEntity.ok(response);
  }

  @PutMapping(value = "/paymentDraft/{paymentDraftId}/approval")
  @PreAuthorize(
      "hasRole('ROLE_AUDITOR') or hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_FINANCIAL_MANAGER')")
  public ResponseEntity<?> auditorApproval(
      @PathVariable("paymentDraftId") int paymentDraftId, Authentication authentication) {
    PaymentDraft draft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(draft)) return failedResponse("PAYMENT_DRAFT_DOES_NOT_EXIST");
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    var role = employee.getRoles().stream().map(x -> x.getName()).findAny();
    EmployeeRole empRole = EmployeeRole.valueOf(role.get());
    PaymentDraft paymentDraft = paymentDraftService.approvePaymentDraft(paymentDraftId, empRole);
    if (Objects.isNull(paymentDraft)) return failedResponse("APPROVAL_FAILED");
    ResponseDTO response = new ResponseDTO("APPROVAL_SUCCESSFUL", SUCCESS, paymentDraft);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "paymentDraft/grnWithoutPayment")
  public ResponseEntity<?> listGRNWithInCompletePayment(@RequestParam PaymentStatus paymentStatus) {
    try {
      List<GoodsReceivedNote> grnList = goodsReceivedNoteService.findGRNWithoutCompletePayment();

      if (grnList.isEmpty()) return notFound("NO_GRN_AWAITING_PAYMENT_FOUND");

      if (grnList.size() > 0 && paymentStatus == PaymentStatus.PARTIAL) {
        List<Payment> partialPay = new ArrayList<>();
        List<GoodsReceivedNote> ppGrn = new ArrayList<>();
        partialPay.addAll(paymentService.findByPaymentStatus(paymentStatus));
        grnList.stream()
            .forEach(
                grn -> {
                  for (Payment p : partialPay) {
                    if (p.getGoodsReceivedNote().getId() == grn.getId()) {
                      ppGrn.add(p.getGoodsReceivedNote());
                    }
                  }
                });
        ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, ppGrn);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("FETCH_FAILED");
  }

  @GetMapping(value = "/paymentDraft")
  public ResponseEntity<?> listDraftsByStatus(
      @RequestParam PaymentStatus status,
      @RequestParam(defaultValue = "0", required = false) @PositiveOrZero int pageNo,
      @RequestParam(defaultValue = "200", required = false) @Positive int pageSize) {
    try {
      List<PaymentDraft> result = paymentDraftService.findByStatus(status, pageNo, pageSize);
      ResponseDTO response = new ResponseDTO("", SUCCESS, result);
      return ResponseEntity.ok(response);
    } catch (Exception e) {

      log.error(e.getMessage());
    }
    return notFound("FETCH_FAILED");
  }
}
