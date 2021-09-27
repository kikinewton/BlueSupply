package com.logistics.supply.repository;

import com.logistics.supply.model.Department;
import com.logistics.supply.model.Floats;
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
public interface FloatsRepository
    extends JpaRepository<Floats, Integer>, JpaSpecificationExecutor<Floats> {

  Optional<Floats> findByFloatRef(String floatRef);

  List<Floats> findByDepartment(Department department);

  @Query(
      value = "Select * from floats f where f.employee_id =:employeeId",
      countQuery = "Select count(f.id) from floats f where f.employee_id =:employeeId",
      nativeQuery = true)
  Page<Floats> findByEmployeeId(@Param("employeeId") int employeeId, Pageable pageable);
}
