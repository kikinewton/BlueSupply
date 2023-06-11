package com.logistics.supply.loader;

import com.logistics.supply.exception.RoleNotFoundException;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.DepartmentRepository;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

  private final EmployeeRepository employeeRepository;
  private final RoleRepository roleRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${superadmin.email}")
  private String superAdminEmail;

  @Override
  @Transactional
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

    if (employeeRepository.count() > 0) return;
    Department department = createDepartment("IT");

    String roleAdmin = "ROLE_ADMIN";
    Role adminRole =
        roleRepository
            .findByName(roleAdmin)
            .orElseThrow(
                () -> new RoleNotFoundException(roleAdmin));

    Employee user = new Employee();
    user.setFirstName("Super");
    user.setLastName("Admin");
    String rawPassword = RandomStringUtils.randomAlphanumeric(10);
    System.out.println("rawPassword = " + rawPassword);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setEmail(superAdminEmail);
    user.setPhoneNo("000000000000");
    user.setDepartment(department);
    user.setRoles(Arrays.asList(adminRole));
    user.setEnabled(true);
    employeeRepository.save(user);
  }

  @Transactional
  Department createDepartment(String name) {
    Department dep = departmentRepository.findByName(name);
    if (dep == null){
      dep = new Department();
      dep.setDescription(name);
      dep.setName(name);
      return departmentRepository.save(dep);
    }
    return null;
  }

}
