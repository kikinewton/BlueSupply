package com.logistics.supply.repository;

import com.logistics.supply.model.PettyCashPayment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PettyCashPaymentRepository extends CrudRepository<PettyCashPayment, Long> {
}
