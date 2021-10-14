package com.logistics.supply.repository;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Floats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FloatsRepository
    extends JpaRepository<Floats, Integer>, JpaSpecificationExecutor<Floats> {

  Optional<Floats> findByFloatRef(String floatRef);

  Page<Floats> findByDepartmentAndEndorsementOrderByIdDesc(
      Department department, EndorsementStatus endorsement, Pageable pageable);

  @Query(
      value = "Select * from float f where f.created_by_id =:employeeId ",
      countQuery = "Select count(f.id) from float f where f.created_by_id =:employeeId",
      nativeQuery = true)
  Page<Floats> findByEmployeeId(@Param("employeeId") int employeeId, Pageable pageable);

  Page<Floats> findByCreatedByIdOrderByIdDesc(int employeeId, Pageable pageable);
}
