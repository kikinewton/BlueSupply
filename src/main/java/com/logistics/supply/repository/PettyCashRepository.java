package com.logistics.supply.repository;

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
      value = "Select * from petty_cash where created_by =:employeeId order by id desc",
      countQuery = "Select count(id) from petty_cash where created_by =:employeeId",
      nativeQuery = true)
  Page<PettyCash> findByEmployee(@Param("employeeId") int employeeId, Pageable pageable);

  Page<PettyCash> findByCreatedByIdOrderByIdDesc(int employeeId, Pageable pageable);

  @Query(
      value = "Select * from petty_cash where upper(approval) = 'APPROVED' order by id desc",
      countQuery = "Select count(id) from petty_cash where upper(approval) = 'APPROVED'",
      nativeQuery = true)
  Page<PettyCash> findApprovedPettyCash(Pageable pageable);

  @Query(
      value =
          "select * from petty_cash pc where upper(pc.approval) = 'PENDING' and upper(pc.endorsement) = 'PENDING' and upper(pc.status) = 'PENDING' and department_id =:departmentId order by id desc",
      nativeQuery = true)
  List<PettyCash> findByDepartment(@Param("departmentId") int departmentId);

  Optional<PettyCash> findByPettyCashRef(String pettyCashRef);

  @Query(
      value =
          "select * from petty_cash pc where upper(pc.approval) = 'PENDING' and upper(pc.endorsement) = 'ENDORSED' and upper(pc.status) = 'PENDING'",
      nativeQuery = true)
  List<PettyCash> findEndorsedPettyCash();

  @Query(
      value =
          "SELECT * FROM petty_cash pc where upper(approval) = 'APPROVED' and upper(status) = 'PROCESSED' and paid is false",
      nativeQuery = true)
  List<PettyCash> findPettyCashPending();
  @Query(value = "select count(id) from request_item", nativeQuery = true)
  long countAll();
}
