package com.logistics.supply.repository;


import com.logistics.supply.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    @Query(value = "Select * from employee e where e.email =:employeeEmail", nativeQuery = true)
    Optional<Employee> findByEmail(@Param("employeeEmail") String email);


}
