package com.logistics.supply.repository;

import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.Floats;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloatGRNRepository
    extends CrudRepository<FloatGRN, Long>, JpaSpecificationExecutor<FloatGRN> {

  Optional<FloatGRN> findByFloats(Floats floats);

  @Query(
      value =
          "with hod_cte as ( select * from float_grn fg where fg.approved_by_hod is null), " +
                  "flt_cte as ( select f.id from float f where f.department_id =:departmentId " +
                  "and upper(f.approval) = 'APPROVAL') select * from hod_cte where hod_cte.id in " +
                  "(select fgf.floatgrn_id from float_grn_floats fgf where fgf.floats_id in (select * from flt_cte)) order by hod_cte.id desc",
      nativeQuery = true)
  List<FloatGRN> findFloatGRNPendingApprovalByDepartment(@Param("departmentId") int departmentId);
}
