package com.logistics.supply.repository;

import com.logistics.supply.model.RequestItemComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestItemCommentRepository
    extends JpaRepository<RequestItemComment, Long>, JpaSpecificationExecutor<RequestItemComment> {

  List<RequestItemComment> findByRequestItemId(int requestItemId);

  @Query(
      value =
          "select * from request_item_comment ric where read is false and ric.request_item_id not in " +
                  "(select ri.id from request_item ri where upper(ri.approval) = 'APPROVED' and ri.employee_id =:employeeId)" +
                  " order by ric.id asc",
      nativeQuery = true)
  List<RequestItemComment> findUnReadEmployeeComment(@Param("employeeId") int employeeId);
}
