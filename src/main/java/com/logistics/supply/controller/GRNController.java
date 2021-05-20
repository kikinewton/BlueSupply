package com.logistics.supply.controller;

import com.logistics.supply.dto.GoodsReceivedNoteDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
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
public class GRNController extends AbstractRestService {

  @PostMapping(value = "/goodsReceivedNote")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseDTO<GoodsReceivedNote> addGRN(
      @Valid @RequestBody GoodsReceivedNoteDTO goodsReceivedNote) {
    GoodsReceivedNote grn = new GoodsReceivedNote();
    LocalPurchaseOrder lpoExist =
        localPurchaseOrderService.findLpoById(goodsReceivedNote.getLpo().getId());
    Invoice invoice = invoiceService.findByInvoiceId(goodsReceivedNote.getInvoice().getId());
    if (Objects.isNull(lpoExist))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "LPO DOES NOT EXIST");
    if (Objects.isNull(invoice))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "INVOICE DOES NOT EXIST");
    BeanUtils.copyProperties(goodsReceivedNote, grn);
    grn.setInvoice(invoice);
    grn.setLocalPurchaseOrder(lpoExist);
    GoodsReceivedNote savedGrn = goodsReceivedNoteService.saveGRN(grn);
    if (Objects.isNull(grn)) return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    return new ResponseDTO<>(HttpStatus.OK.name(), savedGrn, SUCCESS);
  }

  @GetMapping(value = "/goodsReceivedNote")
  public ResponseDTO<List<GoodsReceivedNote>> findAllGRN() {
    List<GoodsReceivedNote> goodsReceivedNotes = goodsReceivedNoteService.findAllGRN();
    if (goodsReceivedNotes.size() >= 0)
      return new ResponseDTO<>(HttpStatus.OK.name(), goodsReceivedNotes, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @GetMapping(value = "/goodsReceivedNote/suppliers/{supplierId}")
  public ResponseDTO<List<GoodsReceivedNote>> findGRNBySupplier(
      @PathVariable("supplierId") int supplierId) {
    List<GoodsReceivedNote> goodsReceivedNotes =
        goodsReceivedNoteService.findBySupplier(supplierId);
    if (goodsReceivedNotes.size() >= 0)
      return new ResponseDTO<>(HttpStatus.OK.name(), goodsReceivedNotes, SUCCESS);
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PutMapping(value = "/goodsReceivedNote/{goodsReceivedNoteId}")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseDTO<GoodsReceivedNote> updateGRN(
      @PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId,
      @Valid @RequestBody GoodsReceivedNoteDTO goodsReceivedNoteDTO) {
    GoodsReceivedNote grn = goodsReceivedNoteService.findGRNById(goodsReceivedNoteId);
    if (Objects.isNull(grn))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "GRN DOES NOT EXIST");
    LocalPurchaseOrder lpo =
        localPurchaseOrderService.findLpoById(goodsReceivedNoteDTO.getLpo().getId());
    if (Objects.isNull(lpo))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "LPO DOES NOT EXIST");
    Invoice invoice = invoiceService.findByInvoiceId(goodsReceivedNoteDTO.getInvoice().getId());
    if (Objects.isNull(invoice))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, "INVOICE DOES NOT EXIST");
    GoodsReceivedNote updatedGrn =
        goodsReceivedNoteService.updateGRN(goodsReceivedNoteId, goodsReceivedNoteDTO);
    if (Objects.isNull(updatedGrn))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    return new ResponseDTO<>(HttpStatus.OK.name(), updatedGrn, SUCCESS);
  }

  @GetMapping(value = "/goodsReceivedNote/{goodsReceivedNoteId}")
  public ResponseDTO<GoodsReceivedNote> findGRNById(
      @PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId) {
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findGRNById(goodsReceivedNoteId);
    if (Objects.isNull(goodsReceivedNote))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    return new ResponseDTO<>(HttpStatus.OK.name(), goodsReceivedNote, SUCCESS);
  }

  @GetMapping(value = "/goodsReceivedNote/invoices/{invoiceNo}")
  public ResponseDTO<GoodsReceivedNote> findByInvoice(@PathVariable("invoiceNo") String invoiceNo) {
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findByInvoice(invoiceNo);
    if (Objects.isNull(goodsReceivedNote))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    return new ResponseDTO<>(HttpStatus.OK.name(), goodsReceivedNote, SUCCESS);
  }

  @GetMapping(value = "/goodsReceivedNote/LPOWithoutGRN")
  public ResponseDTO<List<LocalPurchaseOrder>> findLPOWithoutGRN() {
    List<LocalPurchaseOrder> lpos = new ArrayList<>();
    lpos.addAll(localPurchaseOrderService.findLpoWithoutGRN());
    if (Objects.isNull(lpos)) return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    return new ResponseDTO<>(HttpStatus.OK.name(), lpos, SUCCESS);
  }
}
