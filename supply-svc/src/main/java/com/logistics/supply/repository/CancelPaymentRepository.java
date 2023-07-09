package com.logistics.supply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.CancelPayment;

@Repository
public interface CancelPaymentRepository extends JpaRepository<CancelPayment, Integer> {

}
