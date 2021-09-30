package com.logistics.supply.repository;

import com.logistics.supply.model.Department;
import com.logistics.supply.model.PettyCash;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PettyCashRepository
    extends JpaRepository<PettyCash, Integer>, JpaSpecificationExecutor<PettyCash> {
  @Query(
      value = "Select * from petty_cash where employee_id =:employeeId",
      countQuery = "Select count(id) from petty_cash where employee_id =:employeeId",
      nativeQuery = true)
  Page<PettyCash> findByEmployee(@Param("employeeId") int employeeId, Pageable pageable);

  List<PettyCash> findByDepartment(Department department);

  Optional<PettyCash> findByPettyCashRef(String pettyCashRef);
}
