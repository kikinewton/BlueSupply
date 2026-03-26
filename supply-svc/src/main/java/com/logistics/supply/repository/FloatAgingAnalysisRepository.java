package com.logistics.supply.repository;

import com.logistics.supply.dto.FloatAgingBucket;
import com.logistics.supply.model.FloatAgingAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface FloatAgingAnalysisRepository
    extends ReadOnlyRepository<FloatAgingAnalysis, Integer>,
        JpaSpecificationExecutor<FloatAgingAnalysis> {

  @Query(
      value =
          "select * from float_aging_analysis f where upper(f.requested_by_email) =:requestedByEmail",
      countQuery =
          "select count(*) from float_aging_analysis f where upper(f.requested_by_email) =:requestedByEmail",
      nativeQuery = true)
  Page<FloatAgingAnalysis> findByRequestedByEmail(String requestedByEmail, Pageable pageable);


  @Query(
          value =
                  "select * from float_aging_analysis f where upper(f.staff_id) =:staffId",
          countQuery =
                  "select count(*) from float_aging_analysis f where upper(f.staff_id) =:staffId",
          nativeQuery = true)
  Page<FloatAgingAnalysis> findByStaffId(String staffId, Pageable pageable);

  @Query(
      value =
          "select * from float_aging_analysis f where f.created_date between :startDate and :endDate",
      countQuery =
          "select count(*) from float_aging_analysis f where f.created_date between :startDate and :endDate",
      nativeQuery = true)
  Page<FloatAgingAnalysis> findAllBetweenDate(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

  @Query(
      value =
          "select * from float_aging_analysis f where f.created_date between :startDate and :endDate",
      nativeQuery = true)
  List<Object[]> getAgingAnalysis(
      @Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query(
      value =
          "SELECT CASE " +
          "WHEN ageing_value BETWEEN 0 AND 7 THEN '0-7 days' " +
          "WHEN ageing_value BETWEEN 8 AND 14 THEN '8-14 days' " +
          "WHEN ageing_value BETWEEN 15 AND 30 THEN '15-30 days' " +
          "ELSE '30+ days' END AS bucket, " +
          "COUNT(*) AS count " +
          "FROM float_aging_analysis " +
          "WHERE retired = false " +
          "GROUP BY bucket " +
          "ORDER BY MIN(ageing_value)",
      nativeQuery = true)
  List<FloatAgingBucket> getAgingDistribution();
}
