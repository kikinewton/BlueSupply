package com.logistics.supply.repository;

import com.logistics.supply.model.GoodsReceivedNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GoodsReceivedNoteRepository extends JpaRepository<GoodsReceivedNote, Integer> {
    List<GoodsReceivedNote> findBySupplier(int supplierId);

    GoodsReceivedNote findByInvoiceNo(String invoiceNo);
}

