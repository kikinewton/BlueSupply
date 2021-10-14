package com.logistics.supply.controller;

import com.logistics.supply.auth.AuthService;
import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@RestController
@Slf4j
@RequestMapping("/api")
public class EmployeeController {

  @Autowired EmployeeService employeeService;
  @Autowired private AuthService authService;
  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired private EmployeeRepository employeeRepository;

  @GetMapping("/employees")
  public ResponseEntity<?> getEmployees() {
    try {
      List<Employee> employees = employeeService.getAll();
      if (!employees.isEmpty()) {
        ResponseDTO response = new ResponseDTO("FETCH_ALL_EMPLOYEES", SUCCESS, employees);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  @GetMapping("/employees/{employeeId}")
  public ResponseEntity<?> getEmployeeById(@PathVariable int employeeId) {
    try {
      Employee employee = employeeService.getById(employeeId);
      if (Objects.nonNull(employee)) {
        ResponseDTO response = new ResponseDTO("FETCH_EMPLOYEE_SUCCESSFUL", SUCCESS, employee);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  @GetMapping("/employee")
  public ResponseEntity<?> getEmployeeDetails(Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      if (Objects.nonNull(employee)) {
        ResponseDTO response = new ResponseDTO("FETCH_EMPLOYEE", SUCCESS, employee);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FETCH_FAILED");
  }

  @DeleteMapping("/employees/{employeeId}")
  public ResponseEntity<?> deleteEmployee(@PathVariable Integer employeeId) {
    try {
      Employee employee = employeeService.getById(employeeId);
      if (Objects.nonNull(employee)) {
        employeeService.deleteById(employeeId);
        ResponseDTO response = new ResponseDTO("DELETE_SUCCESSFUL", SUCCESS, null);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("DELETE_FAILED");
  }


  @PutMapping(value = "/employees/{employeeId}")
  public ResponseEntity<?> updateEmployee(
          @RequestBody  EmployeeDTO updateEmployee, @PathVariable int employeeId) {
    try {
      Employee employee = employeeService.getById(employeeId);
      if (Objects.nonNull(employee)) {
        Employee e = employeeService.update(employeeId, updateEmployee);
        ResponseDTO response = new ResponseDTO("EMPLOYEE_UPDATED", SUCCESS, e);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("UPDATE_FAILED");
  }


  @GetMapping(value = "/employees/employeesRelatedToPayment")
  public ResponseEntity<?> findEmployeesRelatedToPayment() {
    Set<Employee> employees = new HashSet<>();
    try {
      employees.addAll(employeeRepository.findEmployeeRelatingToFinance());
      ResponseDTO response =
          new ResponseDTO("FETCH_EMPLOYEES_RELATED_TO_PAYMENT", SUCCESS, employees);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("FAILED_TO_FETCH_EMPLOYEES");
  }

  @PutMapping(value = "/changeActiveState/{employeeId}")
  public ResponseEntity<?> changeEmployeeStatus(@PathVariable("employeeId") int employeeId)
      throws Exception {
    Employee employee = employeeService.findEmployeeById(employeeId);
    Employee result =
        Optional.of(employee)
            .map(
                e -> {
                  e.setEnabled(!e.getEnabled());
                  return employeeService.save(e);
                })
            .orElse(null);
    if (Objects.nonNull(result)) {
      ResponseDTO response = new ResponseDTO("STATUS_CHANGE_SUCCESSFUL", SUCCESS, result);
      return ResponseEntity.ok(response);
    }
    return failedResponse("STATUS_UPDATE_FAILED");
  }

  private ResponseEntity<ResponseDTO> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
