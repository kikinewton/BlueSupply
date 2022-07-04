package com.logistics.supply.repository;

import com.logistics.supply.model.Floats;
import com.logistics.supply.util.Constants;
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

  @Query(
      value = "Select * from float f where f.created_by_id =:employeeId ",
      countQuery = "Select count(f.id) from float f where f.created_by_id =:employeeId",
      nativeQuery = true)
  Page<Floats> findByEmployeeId(@Param("employeeId") int employeeId, Pageable pageable);

  @Query(value = Constants.float_aging_analysis_query, nativeQuery = true)
  List<Object[]> getAgingAnalysis();

  @Query(
      value =
          "select * from float f where upper(status) = 'PROCESSED' and upper(approval) = 'APPROVED' and upper(endorsement) = 'ENDORSED' and funds_received = true and retired = false",
      nativeQuery = true)
  List<Floats> findUnRetiredFloats();

  @Query(value = "select count(id) from float", nativeQuery = true)
  long countAll();
}
