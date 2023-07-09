package com.logistics.supply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.RequestItemComment;
import java.util.List;

@Repository
public interface RequestItemCommentRepository
    extends JpaRepository<RequestItemComment, Long>, JpaSpecificationExecutor<RequestItemComment> {

  List<RequestItemComment> findByRequestItemId(int requestItemId);
  @Query(
      value = "select * from request_item_comment ric where ric.request_item_id = :id",
      nativeQuery = true)
  List<Object[]> getRequestItemCommentHistory(@Param("id") int id);
}
