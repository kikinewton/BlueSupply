package com.logistics.supply.service;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.ApplicationUserRole;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;

import com.logistics.supply.util.CommonHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.logistics.supply.util.CommonHelper.buildEmail;
import static com.logistics.supply.util.CommonHelper.buildNewUserEmail;
import static com.logistics.supply.util.Constants.NEW_EMPLOYEE_CONFIRMATION_MAIL;
import static com.logistics.supply.util.Constants.NEW_USER_PASSWORD_MAIL;

@Service
@Slf4j
@Transactional
@AllArgsConstructor
public class EmployeeService extends AbstractDataService {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailSender emailSender;
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
    boolean employeeExist = employeeRepository.findByEmail(employee.getEmail()).isPresent();

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
    newEmployee.setRoles(ApplicationUserRole.REGULAR.name());
    try {
      return employeeRepository.save(newEmployee);
    } catch (Exception e) {
      e.getMessage();
    }
    return null;
  }

  public Employee signUp(RegistrationRequest request) {
    boolean employeeExist = employeeRepository.findByEmail(request.getEmail()).isPresent();

    if (employeeExist) {
      throw new IllegalStateException("Employee with email already exist");
    }
    Employee newEmployee = new Employee();
    //String password = CommonHelper.generatePassword("b$", 12);
    String password = "password1.com";
    newEmployee.setPassword(bCryptPasswordEncoder.encode(password));
    newEmployee.setEmployeeLevel(request.getEmployeeLevel());
    newEmployee.setDepartment(request.getDepartment());
    newEmployee.setFirstName(request.getFirstName());
    newEmployee.setEmail(request.getEmail());
    newEmployee.setPhoneNo(request.getPhoneNo());
    newEmployee.setLastName(request.getLastName());
    newEmployee.setEnabled(true);
    newEmployee.setRoles(ApplicationUserRole.REGULAR.name());
    newEmployee.setEnabled(false);
    String emailContent =
        buildNewUserEmail(
            request.getLastName().toUpperCase(Locale.ROOT),
            "",
            EmailType.NEW_USER_PASSWORD_MAIL.name(),
            NEW_USER_PASSWORD_MAIL, password);
    Employee result = employeeRepository.save(newEmployee);
    if (Objects.nonNull(result)) {
      emailSender.sendMail(
          "admin@mail.com", request.getEmail(), EmailType.NEW_USER_PASSWORD_MAIL, emailContent);
      return result;
    }
    return null;
  }

  public Employee findEmployeeById(int employeeId) {
    Employee employee = null;
    try {
      return employeeRepository.findById(employeeId).orElseThrow(Exception::new);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public Employee findEmployeeByEmail(String email) {
    Employee employee = null;
    try {
      return employeeRepository.findByEmail(email).orElseThrow(Exception::new);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
