package com.logistics.supply.service;

import com.logistics.supply.dto.GoodsReceivedNoteDTO;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.LocalPurchaseOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class GoodsReceivedNoteService extends AbstractDataService {



  public List<GoodsReceivedNote> findAllGRN() {
    List<GoodsReceivedNote> goodsReceivedNotes = new ArrayList<>();
    try {
      goodsReceivedNotes.addAll(goodsReceivedNoteRepository.findAll());
      return goodsReceivedNotes;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return goodsReceivedNotes;
  }

  public List<GoodsReceivedNote> findBySupplier(int supplierId) {
    List<GoodsReceivedNote> goodsReceivedNotes = new ArrayList<>();
    try {
      goodsReceivedNotes.addAll(goodsReceivedNoteRepository.findBySupplier(supplierId));
      return goodsReceivedNotes;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return goodsReceivedNotes;
  }

  public GoodsReceivedNote findGRNById(int grnId) {
    try {
      return goodsReceivedNoteRepository.findById(grnId).get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public GoodsReceivedNote findByInvoice(String invoiceNo) {
    try {
      return goodsReceivedNoteRepository.findByInvoiceNo(invoiceNo);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNote saveGRN(GoodsReceivedNote goodsReceivedNote) {
    try {
      return goodsReceivedNoteRepository.save(goodsReceivedNote);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNote updateGRN(int grnId, GoodsReceivedNoteDTO grnDto) {
    GoodsReceivedNote grn = findGRNById(grnId);
    LocalPurchaseOrder lpo = localPurchaseOrderRepository.findById(grnDto.getLpo().getId()).get();
    Invoice invoice = invoiceRepository.findById(grnDto.getInvoice().getId()).get();
    BeanUtils.copyProperties(grnDto, grn);
    grn.setLocalPurchaseOrder(lpo);
    grn.setInvoice(invoice);

    try {
      System.out.println("UPDATE GRN");
      return goodsReceivedNoteRepository.save(grn);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
