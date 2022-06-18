package com.logistics.supply.repository;

import com.logistics.supply.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

  Optional<Employee> findByEmailAndEnabledIsTrue(String email);

  Optional<Employee> findByEmail(String email);

  @Query(
      value =
          "SELECT * from employee e join employee_role er on e.id = er.employee_id and er.role_id =:roleId and e.department_id =:departmentId and e.enabled = true order by created_at desc limit 1",
      nativeQuery = true)
  Optional<Employee> findDepartmentHod(@Param("departmentId") int departmentId, @Param("roleId") int roleId);

  @Query(
      value =
          "SELECT * from employee e join employee_role er on e.id = er.employee_id and er.role_id =:roleId and e.enabled = true order by e.id desc limit 1",
      nativeQuery = true)
  Optional<Employee> getGeneralManager(@Param("roleId") int roleId);

  @Query(
      value =
          "SELECT * from employee e join employee_role er on e.id = er.employee_id and er.role_id =:roleId and e.enabled = true order by e.id desc limit 1",
      nativeQuery = true)
  Optional<Employee> findManagerByRoleId(@Param("roleId") int roleId);

  @Query(
      value =
          "select * from employee e where e.id in (select er.employee_id from employee_role er where er.role_id = :roleId) and enabled is true order by created_at desc limit 1",
      nativeQuery = true)
  Optional<Employee> findRecentEmployeeWithRoleId(@Param("roleId") int roleId);

  @Query("UPDATE Employee u SET u.lastLogin=:lastLogin WHERE u.email =:email")
  @Modifying
  @Transactional
  public void updateLastLogin(@Param("lastLogin") Date lastLogin, @Param("email") String email);
}
