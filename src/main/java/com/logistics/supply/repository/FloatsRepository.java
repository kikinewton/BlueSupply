package com.logistics.supply.repository;


import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Floats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloatsRepository extends JpaRepository<Floats, Integer>, JpaSpecificationExecutor<Floats> {

    Optional<Floats> findByFloatRef(String floatRef);

    List<Floats> findByDepartment(Department department);

    List<Floats> findByEmployee(Employee employee);
}
