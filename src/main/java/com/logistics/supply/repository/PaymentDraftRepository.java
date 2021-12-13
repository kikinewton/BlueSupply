package com.logistics.supply.repository;

import com.logistics.supply.model.PaymentDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentDraftRepository extends JpaRepository<PaymentDraft, Integer>, JpaSpecificationExecutor<PaymentDraft> {

//  @Query(
//      value =
//          "UPDATE payment_draft set  approval_from_auditor =:auditorApproval, approval_by_auditor_date = CURRENT_DATE where id =:paymentDraftId",
//      nativeQuery = true)
//  @Modifying
//  @Transactional
//  public void approvePaymentDraft(
//      @Param("auditorApproval") boolean auditorApproval,
//      @Param("paymentDraftId") int paymentDraftId);

}
