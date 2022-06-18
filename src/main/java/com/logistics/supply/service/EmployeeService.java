package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.RoleChangeEvent;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.DepartmentRepository;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.EMPLOYEE_NOT_FOUND;
import static com.logistics.supply.util.Constants.ROLE_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {

  final RoleRepository roleRepository;
  final DepartmentRepository departmentRepository;
  private final EmployeeRepository employeeRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ApplicationEventPublisher applicationEventPublisher;

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

  public Employee disableEmployee(int employeeId) {
    try {
      Optional<Employee> emp =
          employeeRepository
              .findById(employeeId)
              .map(
                  e -> {
                    e.setEnabled(false);
                    return employeeRepository.save(e);
                  });
      if (emp.isPresent()) return emp.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Employee enableEmployee(int employeeId) {
    try {
      Optional<Employee> emp =
          employeeRepository
              .findById(employeeId)
              .map(
                  e -> {
                    e.setEnabled(true);
                    return employeeRepository.save(e);
                  });
      if (emp.isPresent()) return emp.get();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
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
                List<Role> oldRole = employee.getRoles();
                List<Role> roles =
                    updatedEmployee.getRole().stream()
                        .map(r -> roleRepository.findById(r.getId()).get())
                        .collect(Collectors.toList());
                x.setRoles(roles);
                if (!oldRole.contains(roles)) roleChange.set(true);
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
    return employeeRepository.save(newEmployee);
  }

  public Employee changePassword(String password, String email) {
      Employee employee = findEmployeeByEmail(email);
      employee.setPassword(bCryptPasswordEncoder.encode(password));
      return employeeRepository.save(employee);
  }

  @SneakyThrows
  public Employee findEmployeeById(int employeeId) {
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new GeneralException("EMPLOYEE NOT FOUND", HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public Employee findEmployeeByEmail(String email) {
    return employeeRepository
        .findByEmailAndEnabledIsTrue(email)
        .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public Employee getGeneralManager() {
    Role role =
        roleRepository
            .findByName("ROLE_GENERAL_MANAGER")
            .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return employeeRepository
        .getGeneralManager(role.getId())
        .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public Employee getManagerByRoleName(String roleName) {
    Role role =
        roleRepository
            .findByName(roleName)
            .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return employeeRepository
        .findManagerByRoleId(role.getId())
        .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public Employee getDepartmentHOD(Department department) {
    Role r =
        roleRepository
            .findByName("ROLE_HOD")
            .orElseThrow(() -> new GeneralException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
    return employeeRepository
        .findDepartmentHod(department.getId(), r.getId())
        .orElseThrow(() -> new GeneralException(EMPLOYEE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  public Employee findRecentEmployeeWithRoleId(int roleId) {
    return employeeRepository
        .findRecentEmployeeWithRoleId(roleId)
        .orElseThrow(
            () -> new GeneralException("EMPLOYEE WITH ROLE NOT FOUND", HttpStatus.NOT_FOUND));
  }

  public long count() {
    return employeeRepository.count() + 1;
  }
}
