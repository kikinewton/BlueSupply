package com.logistics.supply.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.FloatPayment;

@Repository
public interface FloatPaymentRepository extends CrudRepository<FloatPayment, Long> {}
