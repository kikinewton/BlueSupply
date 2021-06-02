package com.logistics.supply.repository;

import com.logistics.supply.model.PaymentDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentDraftRepository extends JpaRepository<PaymentDraft, Integer> {

  @Query(
      value =
          "UPDATE payment_draft pd set pd.auditor_comment =:comment, pd.approval_from_auditor =:auditorApproval, pd.approval_by_auditor_date = CURRENT_DATE() where pd.id =:paymentDraftId",
      nativeQuery = true)
  @Modifying
  @Transactional
  public void approvePaymentDraft(
      @Param("comment") String comment,
      @Param("auditorApproval") boolean auditorApproval,
      @Param("paymentDraftId") int paymentDraftId);
}
