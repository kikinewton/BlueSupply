package com.logistics.supply.repository;

import com.logistics.supply.model.Employee;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

  Optional<Employee> findByEmailAndEnabledIsTrue(String email);

  Optional<Employee> findByEmail(String email);

  @Query(
      value =
          "SELECT * from employee e join employee_role er on e.id = er.employee_id and er.role =:roleId and e.department_id =:departmentId",
      nativeQuery = true)
  Employee findDepartmentHod(@Param("departmentId") int departmentId, @Param("roleId") int roleId);

  @Query(
      value =
          "SELECT * from employee e join employee_role er on e.id = er.employee_id and er.role =:roleId",
      nativeQuery = true)
  Employee getGeneralManager(@Param("roleId") int roleId);

  @Query("UPDATE Employee u SET u.lastLogin=:lastLogin WHERE u.email =:email")
  @Modifying
  @Transactional
  public void updateLastLogin(@Param("lastLogin") Date lastLogin, @Param("email") String email);

  @Query(
      value =
          "Select * from employee e join employee_role er on er.employee_id = e.id and er.role in (2, 5, 6) and e.enabled = 1",
      nativeQuery = true)
  Set<Employee> findEmployeeRelatingToFinance();
}
