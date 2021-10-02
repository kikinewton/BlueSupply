package com.logistics.supply.controller;

import com.logistics.supply.dto.GoodsReceivedNoteDTO;
import com.logistics.supply.dto.ReceiveGoodsDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class GRNController {

  @Autowired RequestItemService requestItemService;
  @Autowired LocalPurchaseOrderService localPurchaseOrderService;
  @Autowired InvoiceService invoiceService;
  @Autowired GoodsReceivedNoteService goodsReceivedNoteService;
  @Autowired RequestDocumentService requestDocumentService;

  @PostMapping(value = "/goodsReceivedNote")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  public ResponseEntity<?> addGRN(@Valid @RequestBody GoodsReceivedNoteDTO goodsReceivedNote) {
    GoodsReceivedNote grn = new GoodsReceivedNote();
    LocalPurchaseOrder lpoExist =
        localPurchaseOrderService.findLpoById(goodsReceivedNote.getLpo().getId());
    Invoice invoice = invoiceService.findByInvoiceId(goodsReceivedNote.getInvoice().getId());
    if (Objects.nonNull(lpoExist) && Objects.nonNull(invoice)) {

      BeanUtils.copyProperties(goodsReceivedNote, grn);
      grn.setInvoice(invoice);
      grn.setLocalPurchaseOrder(lpoExist);
      GoodsReceivedNote savedGrn = goodsReceivedNoteService.saveGRN(grn);
      if (Objects.nonNull(savedGrn)) {
        ResponseDTO response = new ResponseDTO("SAVE_SUCCESSFUL", SUCCESS, savedGrn);
        return ResponseEntity.ok(response);
      }
      return failedResponse("SAVE_GRN_FAILED");
    }
    if (Objects.isNull(invoice)) return failedResponse("INVOICE_DOES_NOT_EXIST");
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
      @RequestParam(required = false, defaultValue = "NA") String status,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    if (!status.equals("NA")) {
      List<GoodsReceivedNote> grnList = goodsReceivedNoteService.findGRNWithoutCompletePayment();
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, grnList);
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
    return failedResponse("FETCH_FAILED");
  }

  @GetMapping(value = "/goodsReceivedNote/suppliers/{supplierId}")
  public ResponseEntity<?> findGRNBySupplier(@PathVariable("supplierId") int supplierId) {
    List<GoodsReceivedNote> goodsReceivedNotes =
        goodsReceivedNoteService.findBySupplier(supplierId);
    if (!goodsReceivedNotes.isEmpty()) {
      ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, goodsReceivedNotes);
      return ResponseEntity.ok(response);
    }
    return failedResponse("FETCH_FAILED");
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

  @GetMapping(value = "/goodsReceivedNote/LPOWithoutGRN")
  public ResponseEntity<?> findLPOWithoutGRN() {
    List<LocalPurchaseOrder> lpos = new ArrayList<>();
    lpos.addAll(localPurchaseOrderService.findLpoWithoutGRN());
    ResponseDTO response = new ResponseDTO("FETCH_SUCCESSFUL", SUCCESS, lpos);
    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/receiveGoods")
  @PreAuthorize("hasRole('ROLE_STORE_OFFICER')")
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<?> receiveRequestItems(@RequestBody @Valid ReceiveGoodsDTO receiveGoods) {
    System.out.println("create goods received note");
    try {
      System.out.println("run check");
      Set<RequestItem> result =
          receiveGoods.getRequestItems().stream()
              .filter(
                  r ->
                      (Objects.nonNull(r.getUnitPrice())
                              && Objects.nonNull(r.getRequestCategory())
                              && Objects.nonNull(r.getSuppliedBy()))
                          && r.getApproval().equals(RequestApproval.APPROVED))
              .map(
                  i -> {
                    RequestItem item = requestItemService.findById(i.getId()).get();
                    item.setReceivedStatus(true);
                    item.setQuantityReceived(i.getQuantityReceived());
                    item.setInvoiceUnitPrice(i.getInvoiceUnitPrice());
                    return requestItemService.saveRequestItem(item);
                  })
              .collect(Collectors.toSet());
      if (result.size() > 0) {

        System.out.println("check complete");
        boolean docExist =
            requestDocumentService.verifyIfDocExist(
                receiveGoods.getInvoice().getInvoiceDocument().getId());
        if (!docExist) return failedResponse("INVOICE_DOCUMENT_DOES_NOT_EXIST");
        Invoice inv = new Invoice();
        BeanUtils.copyProperties(receiveGoods.getInvoice(), inv);
        Invoice i = invoiceService.saveInvoice(inv);

        System.out.println("invoice created  = " + i);

        if (Objects.nonNull(i)) {
          GoodsReceivedNote grn = new GoodsReceivedNote();
          LocalPurchaseOrder lpoExist =
              localPurchaseOrderService.findLpoById(receiveGoods.getLocalPurchaseOrder().getId());
          if (Objects.isNull(lpoExist)) return failedResponse("LPO_DOES_NOT_EXIST");

          System.out.println("lpo exist");
          grn.setSupplier(i.getSupplier().getId());
          grn.setInvoice(i);
          grn.setReceivedItems(result);
          grn.setLocalPurchaseOrder(lpoExist);
          grn.setComment(receiveGoods.getComment());
          grn.setInvoiceAmountPayable(receiveGoods.getInvoiceAmountPayable());

          GoodsReceivedNote savedGrn = goodsReceivedNoteService.saveGRN(grn);

          if (Objects.nonNull(savedGrn)) {
            System.out.println("savedGrn saved = " + savedGrn);
            ResponseDTO response = new ResponseDTO("GRN_CREATED", SUCCESS, savedGrn);
            return ResponseEntity.ok(response);
          }
        }

        return failedResponse("ERROR_CREATING_INVOICE");
      }
      return failedResponse("REQUEST_HAS_NOT_BEEN_APPROVED_OR_PROPERLY_PROCESSED");
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("");
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

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
