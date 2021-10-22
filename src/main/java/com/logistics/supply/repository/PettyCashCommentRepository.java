package com.logistics.supply.repository;

import com.logistics.supply.model.PettyCashComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PettyCashCommentRepository
    extends JpaRepository<PettyCashComment, Long>, JpaSpecificationExecutor<PettyCashComment> {

  List<PettyCashComment> findByPettyCashIdOrderByIdDesc(int pettyCashId);
}
