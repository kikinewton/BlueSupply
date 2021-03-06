package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.model.Employee;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
public class EmployeeService extends AbstractDataService {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final String EMPLOYEE_NOT_FOUND = "Employee not found";

  public List<Employee> getAll() {
    log.info("Get all employees");
    List<Employee> employees = new ArrayList<>();
    try {
      List<Employee> employeeList = employeeRepository.findAll();
      employees.addAll(employeeList);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return employees;
  }

  public Employee getById(int employeeId) {
    Optional<Employee> employee = employeeRepository.findById(employeeId);
    try {
      return employee.orElseThrow(Exception::new);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void deleteById(int employeeId) {
    try {
      employeeRepository.deleteById(employeeId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Employee create(Employee employee) {
    try {
      return employeeRepository.save(employee);

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
    employee.setUpdatedAt(new Date());
    employee.setEmployeeLevel(updatedEmployee.getEmployeeLevel());
    employee.setDepartment(updatedEmployee.getDepartment());
    try {

      Employee saved = employeeRepository.save(employee);
      return saved;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }



  public Employee signUp(EmployeeDTO employee) {
    boolean employeeExist =
        employeeRepository.findByEmail(employee.getEmail()).isPresent();

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
      return employeeRepository.save(newEmployee);
    } catch (Exception e) {
      e.getMessage();
    }
    return null;
  }

    public Employee findEmployeeById(int employeeId) {
    Employee employee = null;
        try {
          return employeeRepository.findById(employeeId).orElseThrow(Exception::new);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        return  null;
    }

  public Employee findEmployeeByEmail(String  email) {
    Employee employee = null;
    try {
      return employeeRepository.findByEmail(email).orElseThrow(Exception::new);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return  null;
  }

}
