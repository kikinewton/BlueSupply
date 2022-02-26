package com.logistics.supply.controller;

import com.logistics.supply.auth.AuthService;
import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.PasswordResetDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.VerificationType;
import com.logistics.supply.event.listener.EmployeeDisabledEventListener;
import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.VerificationTokenService;
import com.logistics.supply.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

@RestController
@Slf4j
public class EmployeeController {

  @Autowired private EmployeeService employeeService;
  @Autowired private AuthService authService;
  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
  @Autowired private ApplicationEventPublisher applicationEventPublisher;
  @Autowired private EmployeeRepository employeeRepository;
  @Autowired private VerificationTokenService verificationTokenService;

  @GetMapping("/api/employees")
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

  @GetMapping("/api/employees/{employeeId}")
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

  @GetMapping("/api/employee")
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

  @DeleteMapping("/api/employees/{employeeId}")
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

  @PutMapping(value = "/api/employees/{employeeId}")
  public ResponseEntity<?> updateEmployee(
      @RequestBody EmployeeDTO updateEmployee, @PathVariable int employeeId) {
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

  @PutMapping(value = "/api/employees/{employeeId}/disable")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> disableEmployee(@PathVariable int employeeId) {
    try {
      Employee employee = employeeService.disableEmployee(employeeId);
      if (Objects.nonNull(employee)) {
        ResponseDTO response = new ResponseDTO("EMPLOYEE_DISABLED", SUCCESS, employee);
        CompletableFuture.runAsync(
            () -> {
              EmployeeDisabledEventListener.EmployeeDisableEvent disableEvent =
                  new EmployeeDisabledEventListener.EmployeeDisableEvent(this, employee);
              applicationEventPublisher.publishEvent(disableEvent);
            });

        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("DISABLE_EMPLOYEE_FAILED");
  }

  @PutMapping(value = "/api/employees/{employeeId}/enable")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> enableEmployee(@PathVariable int employeeId) {
    try {
      Employee employee = employeeService.enableEmployee(employeeId);
      if (Objects.nonNull(employee)) {
        ResponseDTO response = new ResponseDTO("EMPLOYEE_ENABLED", SUCCESS, employee);
        return ResponseEntity.ok(response);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("ENABLE_EMPLOYEE_FAILED");
  }

  @PostMapping(value = "/resetPassword")
  public ResponseEntity<?> changeEmployeePassword(Authentication authentication) throws Exception {
    if (authentication == null) return failedResponse("Auth token is required");
    boolean verificationSent =
        verificationTokenService.generateVerificationToken(
            authentication.getName(), VerificationType.PASSWORD_RESET);
    if (verificationSent) {
      ResponseDTO response = new ResponseDTO("VERIFICATION_CODE_SENT_TO_EMAIL", SUCCESS, "");
      return ResponseEntity.ok(response);
    }
    return failedResponse("FAILED_TO_SEND_VERIFICATION_CODE");
  }

  @PostMapping(value = "/resetPasswordConfirmation")
  public ResponseEntity<?> resetPasswordConfirmation(@RequestBody PasswordResetDTO passwordResetDTO)
      throws Exception {
    if (passwordResetDTO.getEmail() == null
        || CommonHelper.isValidEmailAddress(passwordResetDTO.getEmail()))
      return failedResponse("Email is invalid");
    boolean confirmTokenValid =
        verificationTokenService.confirmVerificationCode(
            passwordResetDTO.getToken(), passwordResetDTO.getEmail());
    if (!confirmTokenValid) return failedResponse("CHANGE_PASSWORD_FAILED");
    if (StringUtils.isEmpty(passwordResetDTO.getNewPassword()))
      return failedResponse("Password can not be blank");
    Employee employee =
        employeeService.changePassword(
            passwordResetDTO.getNewPassword(), passwordResetDTO.getEmail());
    if (Objects.nonNull(employee)) {
      ResponseDTO response = new ResponseDTO("CHANGE_PASSWORD_SUCCESSFUL", SUCCESS, employee);
      return ResponseEntity.ok(response);
    }
    return failedResponse("CHANGE_PASSWORD_FAILED");
  }

  @PutMapping(value = "/api/changeActiveState/{employeeId}")
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
}
