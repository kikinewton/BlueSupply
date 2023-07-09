package com.logistics.supply.repository;

import com.logistics.supply.model.PaymentDraftComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentDraftCommentRepository extends JpaRepository<PaymentDraftComment, Integer> {

  List<PaymentDraftComment> findByPaymentDraftId(Integer id);
}
