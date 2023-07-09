package com.logistics.supply.repository;

import com.logistics.supply.model.Floats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.FloatGRN;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloatGRNRepository
    extends CrudRepository<FloatGRN, Long>, JpaSpecificationExecutor<FloatGRN> {
  Optional<FloatGRN> findByFloats(Floats floats);

  @Query(
      value =
          "SELECT * FROM float_grn f WHERE f.approved_by_store_manager = true AND f.float_order_id IN " +
                  "(SELECT id FROM float_order fo WHERE fo.retired = false)",
      nativeQuery = true)
  List<FloatGRN> findByApprovedByStoreManagerIsTrueAndNotRetired();

  @Query(
      value =
          "SELECT * FROM float_grn fg WHERE fg.approved_by_store_manager IS false AND fg.employee_store_manager is null AND upper(status) = 'PENDING' OR upper(status) = 'COMMENT' " +
                  "AND EXISTS(SELECT fo.id FROM float_order fo WHERE fo.department_id =:departmentId AND fo.id = fg.float_order_id)",
      nativeQuery = true)
  List<FloatGRN> findPendingApproval(@Param("departmentId") int departmentId);

  Page<FloatGRN> findByCreatedByDepartmentId(int departmentId, Pageable pageable);
}
