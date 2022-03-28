package com.logistics.supply.repository;

import com.logistics.supply.model.CancelPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelPaymentRepository extends JpaRepository<CancelPayment, Integer> {

}
