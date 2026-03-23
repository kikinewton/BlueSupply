package com.logistics.supply.repository;

import com.logistics.supply.interfaces.projections.CancellationRateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.CancelledRequestItem;

import java.util.List;

@Repository
public interface CancelledRequestItemRepository extends JpaRepository<CancelledRequestItem, Integer> {

    @Query(
        value =
            "SELECT (SELECT d.name FROM department d WHERE d.id = ri.user_department) AS department, "
                + "COUNT(ri.id) AS totalRequests, "
                + "COUNT(cri.id) AS cancelledCount "
                + "FROM request_item ri "
                + "LEFT JOIN cancelled_request_item cri ON cri.request_item_id = ri.id "
                + "WHERE ri.deleted = false "
                + "GROUP BY ri.user_department",
        nativeQuery = true)
    List<CancellationRateProjection> findCancellationRateByDepartment();
}

