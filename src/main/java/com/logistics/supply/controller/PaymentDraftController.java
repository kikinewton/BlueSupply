package com.logistics.supply.controller;

import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class PaymentDraftController {

  private final GoodsReceivedNoteService goodsReceivedNoteService;
  private final PaymentDraftService paymentDraftService;
  private final EmployeeService employeeService;
  private final RoleService roleService;
  private final PaymentService paymentService;

  @PostMapping(value = "/paymentDraft")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> savePaymentDraft(
      @Valid @RequestBody PaymentDraftDTO paymentDraftDTO, Authentication authentication) {
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteService.findGRNById(
            Objects.requireNonNull(
                paymentDraftDTO.getGoodsReceivedNote().getId(), "GRN CAN NOT BE NULL"));
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("GRN IS INVALID");
    PaymentDraft paymentDraft = new PaymentDraft();
    BeanUtils.copyProperties(paymentDraftDTO, paymentDraft);
    paymentDraft.setGoodsReceivedNote(goodsReceivedNote);
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    paymentDraft.setCreatedBy(employee);
    try {
      PaymentDraft saved = paymentDraftService.savePaymentDraft(paymentDraft);
      if (saved == null) return failedResponse("PAYMENT DRAFT NOT ADDED");
      ResponseDTO response = new ResponseDTO("PAYMENT DRAFT ADDED", SUCCESS, saved);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("PAYMENT DRAFT FAILED");
  }

  @PutMapping(value = "/paymentDraft/{paymentDraftId}")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> updatePaymentDraft(
      @PathVariable("paymentDraftId") int paymentDraftId,
      @Valid @RequestBody PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    PaymentDraft draft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(draft)) return failedResponse("PAYMENT DRAFT DOES NOT EXIST");
    PaymentDraft paymentDraft =
        paymentDraftService.updatePaymentDraft(paymentDraftId, paymentDraftDTO);
    if (Objects.isNull(paymentDraft)) return failedResponse("UPDATE PAYMENT DRAFT FAILED");
    ResponseDTO response =
        new ResponseDTO<>("UPDATE PAYMENT DRAFT SUCCESSFUL", SUCCESS, paymentDraft);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/paymentDraft/{paymentDraftId}")
  public ResponseEntity<?> findDraftById(@PathVariable("paymentDraftId") int paymentDraftId) {
    PaymentDraft paymentDraft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(paymentDraft)) return failedResponse("PAYMENT DRAFT NOT FOUND");
    ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, paymentDraft);
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
    if (drafts.isEmpty()) return notFound("NO PAYMENT DRAFT FOUND");
    ResponseDTO response = new ResponseDTO("FETCH PAYMENT DRAFT SUCCESSFUL", SUCCESS, drafts);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/paymentDrafts/history")
  @PreAuthorize(
      "hasRole('ROLE_AUDITOR') or hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_FINANCIAL_MANAGER') or hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> paymentDraftHistory(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      Authentication authentication) {
    if (authentication == null) return failedResponse("Auth token required");
    EmployeeRole employeeRole = roleService.getEmployeeRole(authentication);

    Page<PaymentDraft> drafts =
        paymentDraftService.paymentDraftHistory(pageNo, pageSize, employeeRole);
    if (drafts == null || drafts.isEmpty()) return notFound("NO PAYMENT DRAFT FOUND");
    return pagedResult(drafts);
  }

  @PutMapping(value = "/paymentDraft/{paymentDraftId}/approval")
  @PreAuthorize(
      "hasRole('ROLE_AUDITOR') or hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_FINANCIAL_MANAGER')")
  public ResponseEntity<?> paymentApproval(
      @PathVariable("paymentDraftId") int paymentDraftId, Authentication authentication) {
    PaymentDraft draft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(draft)) return failedResponse("PAYMENT DRAFT DOES NOT EXIST");
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    var role = employee.getRoles().stream().map(x -> x.getName()).findAny();
    EmployeeRole empRole = EmployeeRole.valueOf(role.get());
    PaymentDraft paymentDraft = paymentDraftService.approvePaymentDraft(paymentDraftId, empRole);
    if (Objects.isNull(paymentDraft)) return failedResponse("APPROVAL FAILED");
    ResponseDTO response = new ResponseDTO("APPROVAL SUCCESSFUL", SUCCESS, paymentDraft);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "paymentDraft/grnWithoutPayment")
  public ResponseEntity<?> listGRNWithInCompletePayment(@RequestParam PaymentStatus paymentStatus) {
    try {
      List<GoodsReceivedNote> grnList = goodsReceivedNoteService.findGRNWithoutCompletePayment();

      if (grnList.isEmpty()) return notFound("NO GRN AWAITING PAYMENT FOUND");

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
        ResponseDTO response = new ResponseDTO("FETCH SUCCESSFUL", SUCCESS, ppGrn);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return notFound("FETCH FAILED");
  }

  private ResponseEntity<?> pagedResult(Page<PaymentDraft> drafts) {
    PagedResponseDTO.MetaData metaData =
        new PagedResponseDTO.MetaData(
            drafts.getNumberOfElements(),
            drafts.getPageable().getPageSize(),
            drafts.getNumber(),
            drafts.getTotalPages());
    PagedResponseDTO response =
        new PagedResponseDTO("FETCH SUCCESSFUL", SUCCESS, metaData, drafts.getContent());
    return ResponseEntity.ok(response);
  }

}
