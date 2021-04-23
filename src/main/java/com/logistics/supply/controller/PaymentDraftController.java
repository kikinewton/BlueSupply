package com.logistics.supply.controller;

import com.logistics.supply.dto.PaymentDraftDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.PaymentDraft;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
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
    Invoice invoice = invoiceService.findByInvoiceId(paymentDraftDTO.getInvoice().getId());
    if (Objects.isNull(invoice))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    LocalPurchaseOrder localPurchaseOrder =
        localPurchaseOrderService.findLpoById(paymentDraftDTO.getLocalPurchaseOrder().getId());
    if (Objects.isNull(localPurchaseOrder))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteService.findGRNById(paymentDraftDTO.getGoodsReceivedNote().getId());
    if (Objects.isNull(goodsReceivedNote))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    PaymentDraft paymentDraft = new PaymentDraft();
    paymentDraft.setInvoice(invoice);
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
}
