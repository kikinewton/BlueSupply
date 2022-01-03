package com.logistics.supply.repository;

import com.logistics.supply.model.PettyCashOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PettyCashOrderRepository extends CrudRepository<PettyCashOrder, Integer> {
}
