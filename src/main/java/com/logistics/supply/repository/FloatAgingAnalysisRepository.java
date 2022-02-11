package com.logistics.supply.repository;

import com.logistics.supply.model.FloatAgingAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface FloatAgingAnalysisRepository
    extends ReadOnlyRepository<FloatAgingAnalysis, Integer>,
        JpaSpecificationExecutor<FloatAgingAnalysis> {

  @Query(
      value = "select * from float_aging_analysis f where upper(f.requested_by_email) =:requestedByEmail",
      countQuery =
          "select count(*) from float_aging_analysis f where upper(f.requested_by_email) =:requestedByEmail",
      nativeQuery = true)
  Page<FloatAgingAnalysis> findByRequestedByEmail(
      String requestedByEmail, Pageable pageable);

  @Query(
      value =
          "select * from float_aging_analysis f where f.created_date between :startDate and :endDate",
      countQuery =
          "select count(*) from float_aging_analysis f where f.created_date between :startDate and :endDate",
      nativeQuery = true)
  Page<FloatAgingAnalysis> findAllBBetweenDate(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);
}
