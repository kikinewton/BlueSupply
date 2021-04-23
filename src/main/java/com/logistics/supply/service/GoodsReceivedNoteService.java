package com.logistics.supply.service;

import com.logistics.supply.model.GoodsReceivedNote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GoodsReceivedNoteService extends AbstractDataService{

    public List<GoodsReceivedNote> findAllGRN() {
        List<GoodsReceivedNote> goodsReceivedNotes = new ArrayList<>();
        try {
            goodsReceivedNotes.addAll(goodsReceivedNoteRepository.findAll());
            return goodsReceivedNotes;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return goodsReceivedNotes;
    }

    public List<GoodsReceivedNote> findBySupplier(int supplierId) {
        List<GoodsReceivedNote> goodsReceivedNotes = new ArrayList<>();
        try {
            goodsReceivedNotes.addAll(goodsReceivedNoteRepository.findBySupplier(supplierId));
            return goodsReceivedNotes;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return goodsReceivedNotes;
    }

    public GoodsReceivedNote findGRNById(int grnId) {
        try {
            return goodsReceivedNoteRepository.findById(grnId).get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public GoodsReceivedNote findByInvoice(String invoiceNo) {
        try {
            return goodsReceivedNoteRepository.findByInvoiceNo(invoiceNo);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public GoodsReceivedNote saveGRN(GoodsReceivedNote goodsReceivedNote) {
        try{
            return goodsReceivedNoteRepository.save(goodsReceivedNote);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
