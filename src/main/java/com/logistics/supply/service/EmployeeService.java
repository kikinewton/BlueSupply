package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.event.RoleChangeEvent;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.DepartmentRepository;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {

  final RoleRepository roleRepository;
  final DepartmentRepository departmentRepository;
  private final EmployeeRepository employeeRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final EmailSender emailSender;

  public List<Employee> getAll() {
    List<Employee> employees = new ArrayList<>();
    try {
      employees.addAll(employeeRepository.findAll());
      return employees;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return employees;
  }

  public Employee save(Employee employee) {
    try {
      return employeeRepository.save(employee);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Employee getById(int employeeId) {
    Optional<Employee> employee = employeeRepository.findById(employeeId);
    try {
      return employee.orElseThrow(Exception::new);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public void deleteById(int employeeId) {
    try {
      employeeRepository.deleteById(employeeId);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  public Employee create(Employee employee) {
    try {
      return employeeRepository.save(employee);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Employee update(int employeeId, EmployeeDTO updatedEmployee) {
    Employee employee = getById(employeeId);
    AtomicBoolean roleChange = new AtomicBoolean(false);
    return Optional.ofNullable(employee)
        .map(
            x -> {
              if (Objects.nonNull(updatedEmployee.getEmail()))
                x.setEmail(updatedEmployee.getEmail());
              if (Objects.nonNull(updatedEmployee.getFirstName()))
                x.setFirstName(updatedEmployee.getFirstName());
              if (Objects.nonNull(updatedEmployee.getLastName()))
                x.setLastName(updatedEmployee.getLastName());
              if (Objects.nonNull(updatedEmployee.getPhoneNo()))
                x.setPhoneNo(updatedEmployee.getPhoneNo());
              if (Objects.nonNull(updatedEmployee.getDepartment())) {
                Optional<Department> d =
                    departmentRepository.findById(updatedEmployee.getDepartment().getId());
                x.setDepartment(d.get());
              }
              if (!updatedEmployee.getRole().isEmpty()) {
                List<Role> roles =
                    updatedEmployee.getRole().stream()
                        .map(r -> roleRepository.findById(r.getId()).get())
                        .collect(Collectors.toList());
                x.setRoles(roles);
                roleChange.set(true);
              }
              x.setUpdatedAt(new Date());
              try {
                Employee e = employeeRepository.save(x);
                if (e != null && roleChange.get()) {
                  RoleChangeEvent event = new RoleChangeEvent(this, e, roleChange.get());
                  applicationEventPublisher.publishEvent(event);
                }
                return e;
              } catch (Exception e) {
                log.error(e.getMessage());
              }
              return null;
            })
        .orElse(null);
  }

  public Employee signUp(RegistrationRequest request) {
    Employee newEmployee = new Employee();
    String password = "password1.com";
    newEmployee.setPassword(bCryptPasswordEncoder.encode(password));
    newEmployee.setDepartment(request.getDepartment());
    newEmployee.setFirstName(request.getFirstName());
    newEmployee.setEmail(request.getEmail());
    newEmployee.setPhoneNo(request.getPhoneNo());
    newEmployee.setLastName(request.getLastName());

    newEmployee.setRoles(request.getEmployeeRole());
    newEmployee.setEnabled(true);

    Employee result = employeeRepository.save(newEmployee);
    if (Objects.nonNull(result)) {
      return result;
    }
    return null;
  }

  public Employee findEmployeeById(int employeeId) {
    try {
      return employeeRepository.findById(employeeId).get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Employee findEmployeeByEmail(String email) {
    try {
      Optional<Employee> optionalEmployee = employeeRepository.findByEmailAndEnabledIsTrue(email);
      if (optionalEmployee.isPresent()) return optionalEmployee.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Employee getGeneralManager() {
    try {
      Role role = roleRepository.findByName("ROLE_GENERAL_MANAGER");
      System.out.println("role = " + role);
      Employee employee = employeeRepository.getGeneralManager(role.getId());
      System.out.println("employee = " + employee);
      if (Objects.nonNull(employee)) return employee;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Employee getManagerByRoleName(String roleName) {
    try {
      Role role = roleRepository.findByName(roleName);

      Employee employee = employeeRepository.findManagerByRoleId(role.getId());
      System.out.println("employee = " + employee);
      if (Objects.nonNull(employee)) return employee;

    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Employee getDepartmentHOD(Department department) {
    try {
      Role r = roleRepository.findByName("ROLE_HOD");
      if (Objects.isNull(r)) return null;
      return employeeRepository.findDepartmentHod(department.getId(), r.getId());

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public long count() {
    return employeeRepository.count() + 1;
  }
}
