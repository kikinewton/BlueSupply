package com.logistics.supply.controller;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class PaymentDraftController extends AbstractRestService {

  @PostMapping(value = "/paymentDraft")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseDTO<PaymentDraft> savePaymentDraft(
      @Valid @RequestBody PaymentDraftDTO paymentDraftDTO) {
    //    Invoice invoice = invoiceService.findByInvoiceId(paymentDraftDTO.getInvoice().getId());
    //    if (Objects.isNull(invoice))
    //      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteService.findGRNById(
            Objects.requireNonNull(
                paymentDraftDTO.getGoodsReceivedNote().getId(), "GRN CAN NOT BE NULL"));
    if (Objects.isNull(goodsReceivedNote))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    PaymentDraft paymentDraft = new PaymentDraft();
    //    paymentDraft.setInvoice(invoice);
    paymentDraft.setGoodsReceivedNote(goodsReceivedNote);
    BeanUtils.copyProperties(paymentDraftDTO, paymentDraft);
    try {
      var saved = paymentDraftService.savePaymentDraft(paymentDraft);
      return new ResponseDTO<>(HttpStatus.OK.name(), saved, SUCCESS);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PutMapping(value = "/paymentDraft/{paymentDraftId}")
  @PreAuthorize("hasRole('ROLE_ACCOUNT_OFFICER')")
  public ResponseDTO<PaymentDraft> updatePaymentDraft(
      @PathVariable("paymentDraftId") int paymentDraftId,
      @Valid @RequestBody PaymentDraftDTO paymentDraftDTO)
      throws Exception {
    PaymentDraft draft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(draft))
      new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "PAYMENT DRAFT DOES NOT EXIST");
    PaymentDraft paymentDraft =
        paymentDraftService.updatePaymentDraft(paymentDraftId, paymentDraftDTO);
    if (Objects.isNull(paymentDraft))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    return new ResponseDTO<>(HttpStatus.OK.name(), paymentDraft, SUCCESS);
  }

  @GetMapping(value = "/paymentDraft/{paymentDraftId}")
  public ResponseDTO<PaymentDraft> findDraftById(
      @PathVariable("paymentDraftId") int paymentDraftId) {
    PaymentDraft paymentDraft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(paymentDraft))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    return new ResponseDTO<>(HttpStatus.OK.name(), paymentDraft, SUCCESS);
  }

  @GetMapping(value = "/paymentDraft/all")
  public ResponseDTO<List<PaymentDraft>> findPaymentDraft() {
    List<PaymentDraft> drafts = new ArrayList<>();
    drafts.addAll(paymentDraftService.findAllDrafts());
    return new ResponseDTO<>(HttpStatus.OK.name(), drafts, SUCCESS);
  }

  @PutMapping(value = "/paymentDraft/{paymentDraftId}/auditorApproval")
  public ResponseDTO<Payment> auditorApproval(
      @PathVariable("paymentDraftId") int paymentDraftId,
      @RequestParam boolean status,
      @RequestParam String comment) {
    PaymentDraft draft = paymentDraftService.findByDraftId(paymentDraftId);
    if (Objects.isNull(draft))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "PAYMENT DRAFT DOES NOT EXIST");
    Payment payment = paymentDraftService.approvalByAuditor(paymentDraftId, status, comment);
    if (Objects.isNull(payment))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    return new ResponseDTO<>(HttpStatus.OK.name(), payment, SUCCESS);
  }

  @GetMapping(value = "paymentDraft/grnWithoutFullPayment")
  public ResponseDTO<List<GoodsReceivedNote>> findGRNWithoutFullPayment(
      @RequestParam PaymentStatus paymentStatus) {
    try {
      List<GoodsReceivedNote> grnList = goodsReceivedNoteService.findGRNWithoutPayment();
      if (Objects.isNull(paymentStatus)) {
        if (Objects.isNull(grnList))
          return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
        return new ResponseDTO<>(HttpStatus.OK.name(), grnList, SUCCESS);
      }
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
                      System.out.println("p = " + p.getId());
                    }
                  }
                });
        return new ResponseDTO<>(HttpStatus.OK.name(), ppGrn, SUCCESS);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }
}
