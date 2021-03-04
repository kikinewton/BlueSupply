package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.security.PasswordEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@AllArgsConstructor
public class EmployeeService implements UserDetailsService {

  private static final String USER_NOT_FOUND_MSG = "Employee with email %s not found";
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired private AbstractDataService abstractDataService;

  public List<Employee> getAll() {
    log.info("Get all employees");
    List<Employee> employees = new ArrayList<>();
    try {
      List<Employee> employeeList = abstractDataService.employeeRepository.findAll();
      employees.addAll(employeeList);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return employees;
  }

  public Employee getById(int employeeId) {
    Optional<Employee> employee = abstractDataService.employeeRepository.findById(employeeId);
    try {
      return employee.orElseThrow(Exception::new);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void deleteById(int employeeId) {
    try {
      abstractDataService.employeeRepository.deleteById(employeeId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Employee create(Employee employee) {
    try {
      return abstractDataService.employeeRepository.save(employee);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public Employee update(int employeeId, EmployeeDTO updatedEmployee) {
    Employee employee = getById(employeeId);
    employee.setEmail(updatedEmployee.getEmail());
    employee.setFirstName(updatedEmployee.getFirstName());
    employee.setLastName(updatedEmployee.getLastName());
    employee.setPhoneNo(updatedEmployee.getPhoneNo());
    employee.setEnabled(updatedEmployee.getEnabled());
    employee.setUpdatedAt(new Date());
    employee.setEmployeeLevel(updatedEmployee.getEmployeeLevel());
    employee.setDepartment(updatedEmployee.getDepartment());
    try {

      Employee saved = abstractDataService.employeeRepository.save(employee);
      return saved;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return abstractDataService
        .employeeRepository
        .findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
  }

  public Employee signUp(EmployeeDTO employee) {
    boolean employeeExist =
        abstractDataService.employeeRepository.findByEmail(employee.getEmail()).isPresent();

    if (employeeExist) {
      throw new IllegalStateException("Employee with email already exist");
    }
    Employee newEmployee = new Employee();
    String encodedPassword = bCryptPasswordEncoder.encode(employee.getPassword());
    newEmployee.setPassword(encodedPassword);
    newEmployee.setEmployeeLevel(employee.getEmployeeLevel());
    newEmployee.setDepartment(employee.getDepartment());
    newEmployee.setFirstName(employee.getFirstName());
    newEmployee.setEmail(employee.getEmail());
    newEmployee.setPhoneNo(employee.getPhoneNo());
    newEmployee.setLastName(employee.getLastName());
    newEmployee.setEnabled(true);
    try {
      return abstractDataService.employeeRepository.save(newEmployee);
    } catch (Exception e) {
      e.getMessage();
    }
    return null;
  }
}
