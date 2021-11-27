package com.logistics.supply.repository;

import com.logistics.supply.model.FloatPayment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FloatPaymentRepository extends CrudRepository<FloatPayment, Long> {}
