package com.logistics.supply.repository;

import com.logistics.supply.model.FloatGrnComment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloatGRNCommentRepository extends CrudRepository<FloatGrnComment, Long> {

    List<FloatGrnComment> findByFloatGRNId(long id);
}
