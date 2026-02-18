package com.logistics.supply.controller;

import com.logistics.supply.dto.PagedResponseDto;
import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class PaymentDraftController {

  private final GoodsReceivedNoteService goodsReceivedNoteService;
  private final PaymentService paymentService;
  private final EmployeeService employeeService;
  private final RoleService roleService;

  @PostMapping(value = "/paymentDraft")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> savePaymentDraft(
          @Valid @RequestBody PaymentDraftDTO paymentDraftDTO, Authentication authentication)
      throws GeneralException {
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteService.findGRNById(
            Objects.requireNonNull(
                paymentDraftDTO.getGoodsReceivedNote().getId(), "GRN CAN NOT BE NULL"));
    if (Objects.isNull(goodsReceivedNote)) return Helper.failedResponse("GRN IS INVALID");
    Payment payment = new Payment();
    BeanUtils.copyProperties(paymentDraftDTO, payment);
    payment.setGoodsReceivedNote(goodsReceivedNote);
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    payment.setCreatedBy(employee);
    Payment saved = paymentService.savePaymentDraft(payment);
    return ResponseDto.wrapSuccessResult(saved, "PAYMENT DRAFT ADDED");
  }

  @PutMapping(value = "/paymentDraft/{paymentDraftId}")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> updatePaymentDraft(
      @PathVariable("paymentDraftId") int paymentDraftId,
      @Valid @RequestBody PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    Payment existing = paymentService.findById(paymentDraftId);
    if (Objects.isNull(existing)) return Helper.failedResponse("PAYMENT DRAFT DOES NOT EXIST");
    Payment updated = paymentService.updatePaymentDraft(paymentDraftId, paymentDraftDTO);
    if (Objects.isNull(updated)) return Helper.failedResponse("UPDATE PAYMENT DRAFT FAILED");
    return ResponseDto.wrapSuccessResult(updated, "UPDATE PAYMENT DRAFT SUCCESSFUL");
  }

  @GetMapping(value = "/paymentDraft/{paymentDraftId}")
  public ResponseEntity<?> findDraftById(@PathVariable("paymentDraftId") int paymentDraftId)
      throws GeneralException {
    Payment payment = paymentService.findById(paymentDraftId);
    if (Objects.isNull(payment)) return Helper.failedResponse("PAYMENT DRAFT NOT FOUND");
    return ResponseDto.wrapSuccessResult(payment, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/paymentDrafts")
  @PreAuthorize(
      "hasRole('ROLE_AUDITOR') or hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_FINANCIAL_MANAGER') or hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> listPaymentDrafts(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      Authentication authentication) {
    EmployeeRole employeeRole = roleService.getEmployeeRole(authentication);
    List<Payment> drafts = new ArrayList<>(paymentService.findAllDraftsByRole(pageNo, pageSize, employeeRole));
    if (drafts.isEmpty()) return Helper.notFound("NO PAYMENT DRAFT FOUND");
    return ResponseDto.wrapSuccessResult(drafts, Constants.FETCH_SUCCESSFUL);
  }

  @DeleteMapping("/paymentDrafts/{paymentDraftId}")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> deletePaymentDraft(@PathVariable("paymentDraftId") int paymentDraftId) {
    paymentService.deleteById(paymentDraftId);
    String data = MessageFormat.format("Payment draft with id: {0} deleted", paymentDraftId);
    return ResponseDto.wrapSuccessResult(data, "PAYMENT DRAFT DELETED");
  }

  @GetMapping(value = "/paymentDrafts/history")
  @PreAuthorize(
      "hasRole('ROLE_AUDITOR') or hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_FINANCIAL_MANAGER') or hasRole('ROLE_ACCOUNT_OFFICER') or hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> paymentDraftHistory(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      @RequestParam Optional<Boolean> all,
      Authentication authentication)
      throws GeneralException {
    if (authentication == null) return Helper.failedResponse("Auth token required");
    EmployeeRole employeeRole = roleService.getEmployeeRole(authentication);

    if (all.isPresent() && all.get()) {
      Page<Payment> payments =
          paymentService.paymentDraftHistory(pageNo, pageSize, EmployeeRole.ROLE_REGULAR);
      return PagedResponseDto.wrapSuccessResult(payments, Constants.FETCH_SUCCESSFUL);
    }

    Page<Payment> drafts = paymentService.paymentDraftHistory(pageNo, pageSize, employeeRole);
    if (drafts == null || drafts.isEmpty()) return Helper.notFound("NO PAYMENT DRAFT FOUND");
    return PagedResponseDto.wrapSuccessResult(drafts, Constants.FETCH_SUCCESSFUL);
  }

  @PutMapping(value = "/paymentDraft/{paymentDraftId}/approval")
  @PreAuthorize(
      "hasRole('ROLE_AUDITOR') or hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_FINANCIAL_MANAGER')")
  public ResponseEntity<?> paymentApproval(
      @PathVariable("paymentDraftId") int paymentDraftId, Authentication authentication)
      throws GeneralException {
    Payment payment = paymentService.findById(paymentDraftId);
    if (Objects.isNull(payment)) return Helper.failedResponse("PAYMENT DRAFT DOES NOT EXIST");
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    Optional<String> role = employee.getRoles().stream().map(x -> x.getName()).findAny();
    EmployeeRole empRole = EmployeeRole.valueOf(role.get());
    Payment approved = paymentService.approvePaymentDraft(paymentDraftId, empRole);
    if (Objects.isNull(approved)) return Helper.failedResponse("APPROVAL FAILED");
    return ResponseDto.wrapSuccessResult(approved, "APPROVAL SUCCESSFUL");
  }

  @GetMapping(value = "paymentDraft/grnWithoutPayment")
  public ResponseEntity<?> listGRNWithInCompletePayment(@RequestParam PaymentStatus paymentStatus) {
    try {
      List<GoodsReceivedNote> grnList = goodsReceivedNoteService.findGRNWithoutCompletePayment();

      if (grnList.isEmpty()) return Helper.notFound("NO GRN AWAITING PAYMENT FOUND");

      if (paymentStatus == PaymentStatus.PARTIAL) {
        List<Payment> partialPay = new ArrayList<>();
        List<GoodsReceivedNote> ppGrn = new ArrayList<>();
        partialPay.addAll(paymentService.findByPaymentStatus(paymentStatus));
        grnList.forEach(
            grn -> {
              for (Payment p : partialPay) {
                if (p.getGoodsReceivedNote().getId() == grn.getId()) {
                  ppGrn.add(p.getGoodsReceivedNote());
                }
              }
            });
        return ResponseDto.wrapSuccessResult(ppGrn, Constants.FETCH_SUCCESSFUL);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return Helper.notFound("FETCH FAILED");
  }
}
