package com.logistics.supply.repository;

import com.logistics.supply.model.PaymentDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentDraftRepository extends JpaRepository<PaymentDraft, Integer>, JpaSpecificationExecutor<PaymentDraft> {

  @Query(
      value =
          "UPDATE payment_draft set auditor_comment =:comment, approval_from_auditor =:auditorApproval, approval_by_auditor_date = CURRENT_DATE where id =:paymentDraftId",
      nativeQuery = true)
  @Modifying
  @Transactional
  public void approvePaymentDraft(
      @Param("comment") String comment,
      @Param("auditorApproval") boolean auditorApproval,
      @Param("paymentDraftId") int paymentDraftId);
}
