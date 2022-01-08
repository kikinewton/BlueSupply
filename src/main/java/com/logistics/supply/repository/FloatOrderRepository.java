package com.logistics.supply.repository;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloatOrderRepository extends JpaRepository<FloatOrder, Integer>, JpaSpecificationExecutor<FloatOrder> {

    Page<FloatOrder> findByRetired(boolean retired, Pageable pageable);

    Page<FloatOrder> findByCreatedBy(Employee createdBy, Pageable pageable);

    Page<FloatOrder> findByCreatedByIdOrderByIdDesc(int employeeId, Pageable pageable);

    Optional<FloatOrder> findByFloatOrderRef(String floatOrderRef);

    @Query(value = "select * from float_order f where upper(status) = 'PROCESSED' and upper(approval) = 'APPROVED' and upper(endorsement) = 'ENDORSED' and funds_received = true and retired = false", nativeQuery = true)
    List<FloatOrder> findUnRetiredFloats();
}
