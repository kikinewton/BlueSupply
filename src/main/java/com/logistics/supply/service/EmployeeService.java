package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
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

  @Transactional(rollbackFor = Exception.class, readOnly = true)
  public Employee update(int employeeId, EmployeeDTO updatedEmployee) {
    Employee employee = getById(employeeId);
    if (Objects.nonNull(updatedEmployee.getEmail())) employee.setEmail(updatedEmployee.getEmail());
    if (Objects.nonNull(updatedEmployee.getFirstName()))
      employee.setFirstName(updatedEmployee.getFirstName());
    if (Objects.nonNull(updatedEmployee.getLastName()))
      employee.setLastName(updatedEmployee.getLastName());
    if (Objects.nonNull(updatedEmployee.getPhoneNo()))
      employee.setPhoneNo(updatedEmployee.getPhoneNo());
    if (Objects.nonNull(updatedEmployee.getDepartment()))
      employee.setDepartment(updatedEmployee.getDepartment());
    if (!updatedEmployee.getRole().isEmpty()) {
      employee.getRole().clear();
      employee.setRole(updatedEmployee.getRole());
    }
    employee.setUpdatedAt(new Date());
    try {
      return employeeRepository.save(employee);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public Employee changeRole(int employeeId, List<EmployeeRole> roles) {
    Employee employee = findEmployeeById(employeeId);
    try {
      employee.setRole(roles);
      return employeeRepository.save(employee);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Employee signUp(RegistrationRequest request) {
    boolean employeeExist = employeeRepository.findByEmail(request.getEmail()).isPresent();

    if (employeeExist) {
      throw new IllegalStateException("Employee with email already exist");
    }
    Employee newEmployee = new Employee();
    String password = "password1.com";
    newEmployee.setPassword(bCryptPasswordEncoder.encode(password));
    newEmployee.setDepartment(request.getDepartment());
    newEmployee.setFirstName(request.getFirstName());
    newEmployee.setEmail(request.getEmail());
    newEmployee.setPhoneNo(request.getPhoneNo());
    newEmployee.setLastName(request.getLastName());

    newEmployee.setRole(request.getEmployeeRole());
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

  public List<Employee> findAllEmployees() {
    List<Employee> employees = new ArrayList<>();
    try {
      employees.addAll(employeeRepository.findAll());
      return employees;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return employees;
  }

  public Employee getGeneralManager(int roleId) {
    try {
      Employee employee = employeeRepository.getGeneralManager(roleId);
      if (Objects.nonNull(employee)) return employee;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public Employee getDepartmentHOD(Department department) {
    try {
      return employeeRepository.findDepartmentHod(
          department.getId(), EmployeeRole.ROLE_HOD.ordinal());

    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public long count() {
    return employeeRepository.count();
  }
}
