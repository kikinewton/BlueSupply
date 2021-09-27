package com.logistics.supply.controller;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.service.GoodsReceivedNoteService;
import com.logistics.supply.service.PaymentDraftService;
import com.logistics.supply.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class PaymentDraftController {

  @Autowired GoodsReceivedNoteService goodsReceivedNoteService;
  @Autowired PaymentDraftService paymentDraftService;
  @Autowired PaymentService paymentService;

  @PostMapping(value = "/paymentDraft")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseEntity<?> savePaymentDraft(@Valid @RequestBody PaymentDraftDTO paymentDraftDTO) {
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteService.findGRNById(
            Objects.requireNonNull(
                paymentDraftDTO.getGoodsReceivedNote().getId(), "GRN_CAN_NOT_BE_NULL"));
    if (Objects.isNull(goodsReceivedNote)) return failedResponse("");
    PaymentDraft paymentDraft = new PaymentDraft();
    paymentDraft.setGoodsReceivedNote(goodsReceivedNote);
    BeanUtils.copyProperties(paymentDraftDTO, paymentDraft);
    try {
      PaymentDraft saved = paymentDraftService.savePaymentDraft(paymentDraft);
      ResponseDTO response = new ResponseDTO("", SUCCESS, saved);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("SAVE_PAYMENT_DRAFT_FAILED");
  }

  //  @PostMapping(value = "/paymentDraft/pettyCashOrFloat")
  //  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  //  public ResponseDTO<PaymentDraft> allocateMoneyForFloatOrPettyCash(
  //      FloatOrPettyCashDTO floatOrPettyCash) {
  //    PaymentDraft paymentDraft = new PaymentDraft();
  //    paymentDraft.setPaymentAmount(floatOrPettyCash.getPaymentAmount());
  //    paymentDraft.setPaymentMethod(PaymentMethod.CASH);
  //    paymentDraft.setPaymentStatus(PaymentStatus.COMPLETED);
  //    try {
  //      PaymentDraft p = paymentDraftService.savePaymentDraft(paymentDraft);
  //      ResponseDTO response = new ResponseDTO(, SUCCESS,p);
  //    } catch (Exception e) {
  //      e.printStackTrace();
  //    }
  //    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  //  }

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
    if (Objects.isNull(paymentDraft)) return failedResponse("UPDATE_FAILED");
    ResponseDTO response = new ResponseDTO<>("UPDATE_SUCCESSFUL", SUCCESS, paymentDraft);
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
  public ResponseEntity<?> findPaymentDraft(
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    List<PaymentDraft> drafts = new ArrayList<>();

    drafts.addAll(paymentDraftService.findAllDrafts(pageNo, pageSize));
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, drafts);
    return ResponseEntity.ok(response);
  }

  @PutMapping(value = "/paymentDraft/{paymentDraftId}/auditorApproval")
  @PreAuthorize("hasRole('ROLE_AUDITOR')")
  public ResponseEntity<?> auditorApproval(
      @PathVariable("paymentDraftId") int paymentDraftId, @RequestParam boolean status) {
    PaymentDraft draft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(draft)) return failedResponse("PAYMENT_DRAFT_DOES_NOT_EXIST");

    System.out.println("status = " + status);
    Payment payment = paymentDraftService.approvePaymentDraft(paymentDraftId, status);
    if (Objects.isNull(payment)) return failedResponse("APPROVAL_FAILED");
    ResponseDTO response = new ResponseDTO("APPROVAL_SUCCESSFUL", SUCCESS, payment);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "paymentDraft/grnWithoutPayment")
  public ResponseEntity<?> findGRNWithoutCompletePayment(
      @RequestParam PaymentStatus paymentStatus) {
    try {
      List<GoodsReceivedNote> grnList = goodsReceivedNoteService.findGRNWithoutCompletePayment();

      if (grnList.isEmpty()) return failedResponse("LIST_IS_EMPTY");

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
    return failedResponse("FETCH_FAILED");
  }

  @GetMapping(value = "/paymentDraft")
  public ResponseEntity<?> findDraftByStatus(
      @RequestParam PaymentStatus status,
      @RequestParam(defaultValue = "0", required = false) @PositiveOrZero int pageNo,
      @RequestParam(defaultValue = "50", required = false) @Positive int pageSize) {
    try {
      List<PaymentDraft> result = paymentDraftService.findByStatus(status, pageNo, pageSize);
      ResponseDTO response = new ResponseDTO("", SUCCESS, result);
      return ResponseEntity.ok(response);
    } catch (Exception e) {

      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
