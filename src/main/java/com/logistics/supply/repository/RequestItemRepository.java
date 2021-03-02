package com.logistics.supply.repository;

import com.logistics.supply.model.RequestItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RequestItemRepository extends JpaRepository<RequestItem, Integer> {

    Page<RequestItem> findAll(Pageable pageable);

    @Query(value = "Select * from RequestItems where Date(requested_date) >=:fromDate", nativeQuery = true)
    List<RequestItem> getRequestBetweenDateAndNow(@Param("fromDate") String fromDate);

    @Query(value = "Select * from RequestItems where approval=:approvalStatus and Date(requested_date) >=:fromDate", nativeQuery = true)
    List<RequestItem> getByApprovalStatus(@Param("approvalStatus") String approvalStatus, @Param("fromDate") String fromDate);

    @Query(value = "Select * from RequestItems where status=:requestStatus and Date(requested_date) >=:fromDate", nativeQuery = true)
    List<RequestItem> getByStatus(@Param("requestStatus") String requestStatus, @Param("fromDate") String fromDate);

    @Query(value = "Select * from RequestItems where reason =:requestReason and Date(requested_date) >=:fromDate", nativeQuery = true)
    List<RequestItem> getByReason(@Param("requestReason") String requestReason, @Param("fromDate") String fromDate);

    @Query(value = "Select * from RequestItems where employee_id=:employeeId", nativeQuery = true)
    Optional<RequestItem> getByEmployeeId(@Param("employeeId") String employeeId);

    @Query(value = "Select * from RequestItem where supplier_id =:supplierId", nativeQuery = true)
    Optional<RequestItem> getBySupplier(@Param("supplierId") String supplierId);
}
