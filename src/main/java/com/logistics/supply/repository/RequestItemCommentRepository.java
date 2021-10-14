package com.logistics.supply.repository;

import com.logistics.supply.model.RequestItemComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestItemCommentRepository
    extends JpaRepository<RequestItemComment, Long>, JpaSpecificationExecutor<RequestItemComment> {

    List<RequestItemComment> findByRequestItemIdOrderByIdDesc(int requestItemId);
}
