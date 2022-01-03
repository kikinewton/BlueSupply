package com.logistics.supply.repository;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;


public interface FloatOrderRepository extends CrudRepository<FloatOrder, Integer> {

    Page<FloatOrder> findByRetired(boolean retired, Pageable pageable);

    Page<FloatOrder> findByCreatedBy(Employee createdBy, Pageable pageable);
}
