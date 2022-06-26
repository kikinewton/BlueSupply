package com.logistics.supply.repository;

import com.logistics.supply.model.FloatComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloatCommentRepository
    extends JpaRepository<FloatComment, Long>, JpaSpecificationExecutor<FloatComment> {
  List<FloatComment> findByFloats_IdEquals(Integer id);

  List<FloatComment> findByReadFalseAndEmployeeId(int employeeId);
}
