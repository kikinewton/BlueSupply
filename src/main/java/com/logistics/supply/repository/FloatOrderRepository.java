package com.logistics.supply.repository;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatOrder;
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
public interface FloatOrderRepository
    extends JpaRepository<FloatOrder, Integer>, JpaSpecificationExecutor<FloatOrder> {

  Page<FloatOrder> findByRetired(boolean retired, Pageable pageable);

  Page<FloatOrder> findByCreatedBy(Employee createdBy, Pageable pageable);

  Page<FloatOrder> findByCreatedByIdOrderByIdDesc(int employeeId, Pageable pageable);

  Optional<FloatOrder> findByFloatOrderRef(String floatOrderRef);

  @Query(
      value =
          "select * from float_order f where upper(status) = 'PROCESSED' and upper(approval) = 'APPROVED' and upper(endorsement) = 'ENDORSED' and funds_received = true and retired = false",
      nativeQuery = true)
  List<FloatOrder> findUnRetiredFloats();

  @Query(value = Constants.float_order_aging_analysis_query, nativeQuery = true)
  List<Object[]> getAgingAnalysis();

  @Query(
      value = Constants.getFloat_order_aging_analysis_query_by_requester_email,
      countQuery = Constants.getFloat_order_aging_analysis_query_by_requester_email_count,
      nativeQuery = true)
  List<Object[]> getAgingAnalysisByEmail(@Param("requested_by_email") String requestedEyEmail, Pageable pageable);

  long countByRetired(boolean retired);
  long countByEndorsement(EndorsementStatus status);
  long countByApproval(RequestApproval approval);
  long countByStatus(RequestStatus status);
  long countByGmRetirementApproval(boolean gmRetirementApproval);
  long countByAuditorRetirementApproval(boolean auditorRetirementApproval);

  @Query(value = "select count(id) from float_order", nativeQuery = true)
  long countAll();

  @Query(
          value =
                  "SELECT * FROM float_order fo WHERE fo.has_document = true AND fo.retired = false AND UPPER(fo.float_type) = 'GOODS' and fo.department_id =:departmentId",
          nativeQuery = true)
  List<FloatOrder> findGoodsFloatOrderRequiringGRN(@Param("departmentId") int departmentId);
}
