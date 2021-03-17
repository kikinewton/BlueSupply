package com.logistics.supply.repository;


import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RequestPerMonthRepository extends JpaRepository<RequestPerCurrentMonthPerDepartment, Integer> {
    static final String REQUEST_PER_DEPARTMENT_FOR_CURRENT_MONTH =
            "select d.id, d.name as Department, count(r.id) as Num_of_Request from department d join employee e on e.department_id = d.id join request_item r on r.employee_id = e.id where MONTH (r.created_date) = MONTH (CURDATE()) and r.employee_id group by d.name, d.id";

    @Query(value = REQUEST_PER_DEPARTMENT_FOR_CURRENT_MONTH, nativeQuery = true)
    List<RequestPerCurrentMonthPerDepartment> getNumOfRequestPerCurrentMonthPerDepartment();
}
