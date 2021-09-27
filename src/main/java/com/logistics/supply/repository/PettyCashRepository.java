package com.logistics.supply.repository;

import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.PettyCash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PettyCashRepository
    extends JpaRepository<PettyCash, Integer>, JpaSpecificationExecutor<PettyCash> {
//  Optional<PettyCash> findByEmployee(Employee employee);

  List<PettyCash> findByDepartment(Department department);

  Optional<PettyCash> findByPettyCashRef(String pettyCashRef);
}
