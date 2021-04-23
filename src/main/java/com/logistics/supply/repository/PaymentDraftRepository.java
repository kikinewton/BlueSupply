package com.logistics.supply.repository;

import com.logistics.supply.model.Payment;
import com.logistics.supply.model.PaymentDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentDraftRepository extends JpaRepository<PaymentDraft, Long> {

}
