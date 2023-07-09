package com.logistics.supply.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.logistics.supply.model.PettyCashPayment;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PettyCashPaymentRepository extends CrudRepository<PettyCashPayment, Long> {

  @Query(
      value =
          "with pc_cte as ( select * from petty_cash pc), emp_cte as ( select * from employee e), dep_cte as ( select * from department d2), pyt_cte as ( select * from petty_cash_payment pcp) select cast(pyt_cte.created_date as date) as payment_date, pc_cte.name as petty_cash_description , pc_cte.petty_cash_ref, pc_cte.purpose, pc_cte.quantity, pc_cte.amount, pc_cte.quantity * pc_cte.amount as total_cost, emp_cte.full_name as requested_by, dep_cte.name as department, (select full_name from emp_cte where emp_cte.id = pyt_cte.paid_by_id) as paid_by from pc_cte inner join emp_cte on pc_cte.created_by = emp_cte.id inner join dep_cte on pc_cte.department_id = dep_cte.id inner join pyt_cte on pc_cte.id = pyt_cte.petty_cash_id where pc_cte.created_date between cast(:startDate as date) and cast(:endDate as date)",
      nativeQuery = true)
  List<Object[]> getPaymentReport(
          @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
