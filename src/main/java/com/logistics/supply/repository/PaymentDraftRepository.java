package com.logistics.supply.repository;

import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.PaymentDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentDraftRepository
    extends JpaRepository<PaymentDraft, Integer>, JpaSpecificationExecutor<PaymentDraft> {

  boolean existsByGoodsReceivedNote(GoodsReceivedNote goodsReceivedNote);

  Optional<PaymentDraft> findByGoodsReceivedNote(GoodsReceivedNote goodsReceivedNote);

  long countByApprovalFromGM(boolean approvalFromGM);

  long countByApprovalFromFM(boolean approvalFromFM);
}
