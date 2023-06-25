package com.logistics.supply.repository;

import com.logistics.supply.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

  Optional<Employee> findByEmailAndEnabledIsTrue(String email);

  Optional<Employee> findByEmail(String email);

  @Query(
      value =
          "SELECT * from employee e join employee_role er on e.id = er.employee_id and er.role_id =:roleId and e.department_id =:departmentId and e.enabled = true order by id desc limit 1",
      nativeQuery = true)
  Optional<Employee> findDepartmentHod(@Param("departmentId") int departmentId, @Param("roleId") int roleId);

  @Query(
      value =
          "SELECT * FROM employee e JOIN employee_role er ON e.id = er.employee_id AND er.role_id =:roleId AND e.enabled = true ORDER BY e.id DESC LIMIT 1",
      nativeQuery = true)
  Optional<Employee> getGeneralManager(@Param("roleId") int roleId);

  @Query(
      value =
          "SELECT * FROM employee e JOIN employee_role er ON e.id = er.employee_id AND er.role_id =:roleId AND e.enabled = true ORDER BY e.id DESC LIMIT 1",
      nativeQuery = true)
  Optional<Employee> findManagerByRoleId(@Param("roleId") int roleId);

  @Query(
      value =
          "SELECT * FROM employee e WHERE e.id IN (SELECT er.employee_id FROM employee_role er WHERE er.role_id = :roleId) AND ENABLED IS TRUE ORDER BY created_at DESC LIMIT 1",
      nativeQuery = true)
  Optional<Employee> findRecentEmployeeWithRoleId(@Param("roleId") int roleId);

  @Query("UPDATE Employee u SET u.lastLogin= NOW() WHERE u.email =:email")
  @Modifying
  @Transactional
  public void updateLastLogin(@Param("email") String email);

  @Query(value = "SELECT COUNT(id) FROM employee", nativeQuery = true)
  long countAll();

  @Query("UPDATE Employee e SET e.enabled= false WHERE e.id =:id and e.deleted = false")
  @Modifying
  @Transactional
    void disableEmployee(@Param("id") int id);
}
