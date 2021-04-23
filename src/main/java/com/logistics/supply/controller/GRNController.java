package com.logistics.supply.controller;

import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public ResponseDTO<GoodsReceivedNote> addGRN(@Valid @RequestBody GoodsReceivedNote goodsReceivedNote) {
        GoodsReceivedNote grn = goodsReceivedNoteService.saveGRN(goodsReceivedNote);
        if (Objects.isNull(grn)) return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
        return new ResponseDTO<>(HttpStatus.OK.name(),  grn, SUCCESS);
    }

    @GetMapping(value = "/goodsReceivedNote")
    public ResponseDTO<List<GoodsReceivedNote>> findAllGRN() {
        List<GoodsReceivedNote> goodsReceivedNotes = goodsReceivedNoteService.findAllGRN();
        if (goodsReceivedNotes.size() >= 0) return new ResponseDTO<>(HttpStatus.OK.name(),  goodsReceivedNotes, SUCCESS);
        return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(),  null, ERROR);
    }

    @GetMapping(value = "/goodsReceivedNote/suppliers/{supplierId}")
    public ResponseDTO<List<GoodsReceivedNote>> findGRNBySupplier(@PathVariable("supplierId") int supplierId) {
        List<GoodsReceivedNote> goodsReceivedNotes = goodsReceivedNoteService.findBySupplier(supplierId);
        if (goodsReceivedNotes.size() >= 0) return new ResponseDTO<>(HttpStatus.OK.name(), goodsReceivedNotes, SUCCESS);
        return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
    }

    @GetMapping(value = "/goodsReceivedNote/{goodsReceivedNoteId}")
    public ResponseDTO<GoodsReceivedNote> findGRNById(@PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId) {
        GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findGRNById(goodsReceivedNoteId);
        if (Objects.isNull(goodsReceivedNote)) return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
        return new ResponseDTO<>(HttpStatus.OK.name(),  goodsReceivedNote, SUCCESS);
    }

    @GetMapping(value = "/goodsReceivedNote/invoices/{invoiceNo}")
    public ResponseDTO<GoodsReceivedNote> findByInvoice(@PathVariable("invoiceNo") String invoiceNo) {
        GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findByInvoice(invoiceNo);
        if (Objects.isNull(goodsReceivedNote)) return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
        return new ResponseDTO<>(HttpStatus.OK.name(),  goodsReceivedNote, SUCCESS);

    }


}
