package com.logistics.supply.controller;

import com.logistics.supply.dto.ChangePasswordDTO;
import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.PasswordResetDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.VerificationType;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.listener.EmployeeDisabledEventListener;
import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.VerificationTokenService;
import com.logistics.supply.util.CommonHelper;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

@RestController
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {
  private final EmployeeService employeeService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final VerificationTokenService verificationTokenService;
  private final EmployeeRepository employeeRepository;

  @GetMapping("/api/employees")
  public ResponseEntity<?> getEmployees() {
    List<Employee> employees = employeeService.getAll();
    return ResponseDTO.wrapSuccessResult(employees, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping("/api/employees/{employeeId}")
  public ResponseEntity<?> getEmployeeById(@PathVariable int employeeId) throws GeneralException {
    Employee employee = employeeService.getById(employeeId);
    return ResponseDTO.wrapSuccessResult(employee, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping("/api/employee")
  public ResponseEntity<?> getEmployeeDetails(Authentication authentication) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    return ResponseDTO.wrapSuccessResult(employee, Constants.FETCH_SUCCESSFUL);
  }

  @DeleteMapping("/api/employees/{employeeId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> deleteEmployee(@PathVariable Integer employeeId) {
    employeeService.deleteById(employeeId);
    return ResponseDTO.wrapSuccessResult(null, "DELETE SUCCESSFUL");
  }

  @PutMapping(value = "/api/employees/{employeeId}")
  public ResponseEntity<?> updateEmployee(
          @RequestBody EmployeeDTO updateEmployee, @PathVariable int employeeId)
      throws GeneralException {
    Employee e = employeeService.update(employeeId, updateEmployee);
    return ResponseDTO.wrapSuccessResult(e, "EMPLOYEE UPDATED");
  }

  @PutMapping(value = "/api/employees/{employeeId}/disable")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> disableEmployee(@PathVariable int employeeId) throws GeneralException {
    Employee employee = employeeService.disableEmployee(employeeId);
    if (Objects.nonNull(employee)) {
      ResponseDTO response = new ResponseDTO("EMPLOYEE DISABLED", Constants.SUCCESS, employee);
      CompletableFuture.runAsync(
          () -> {
            EmployeeDisabledEventListener.EmployeeDisableEvent disableEvent =
                new EmployeeDisabledEventListener.EmployeeDisableEvent(this, employee);
            applicationEventPublisher.publishEvent(disableEvent);
          });
      return ResponseEntity.ok(response);
    }
    return ResponseDTO.wrapErrorResult("DISABLE EMPLOYEE FAILED");
  }

  @PutMapping(value = "/api/employees/{employeeId}/enable")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> enableEmployee(@PathVariable int employeeId) throws GeneralException {
    Employee employee = employeeService.enableEmployee(employeeId);
    return ResponseDTO.wrapSuccessResult(employee, "EMPLOYEE ENABLED");
  }

  @PutMapping(value = "/resetPassword")
  public ResponseEntity<?> changeEmployeePassword(Authentication authentication) throws GeneralException {
    if (authentication == null) return Helper.failedResponse("Auth token is required");
    boolean verificationSent =
        verificationTokenService.generateVerificationToken(
            authentication.getName(), VerificationType.PASSWORD_RESET);
    if (verificationSent) {
      return ResponseDTO.wrapSuccessResult("", "VERIFICATION CODE SENT TO EMAIL");
    }
    return Helper.failedResponse("FAILED TO SEND VERIFICATION CODE");
  }

  @PutMapping(value = "/api/admin/employees/{employeeId}/resetPassword")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<?> resetPasswordByAdmin(@PathVariable("employeeId") int employeeId) throws GeneralException {
    Employee employee = employeeService.findEmployeeById(employeeId);
    if (employee == null) return Helper.failedResponse("EMPLOYEE DOES NOT EXIST");
    boolean verificationSent =
        verificationTokenService.generateVerificationToken(
            employee.getEmail(), VerificationType.PASSWORD_RESET);
    if (verificationSent) {
      return ResponseDTO.wrapSuccessResult(true, "VERIFICATION CODE SENT TO EMAIL");
    }
    return Helper.failedResponse("FAILED TO SEND VERIFICATION CODE");
  }

  @PostMapping(value = "/resetPasswordConfirmation")
  public ResponseEntity<?> resetPasswordConfirmation(@RequestBody PasswordResetDTO passwordResetDTO)
      throws Exception {
    if (passwordResetDTO.getEmail() == null
        || CommonHelper.isValidEmailAddress(passwordResetDTO.getEmail()))
      return Helper.failedResponse("Email is invalid");
    boolean confirmTokenValid =
        verificationTokenService.confirmVerificationCode(
            passwordResetDTO.getToken(), passwordResetDTO.getEmail());
    if (!confirmTokenValid) return Helper.failedResponse("CHANGE PASSWORD FAILED");
    if (StringUtils.isEmpty(passwordResetDTO.getNewPassword()))
      return Helper.failedResponse("Password can not be blank");
    Employee employee =
        employeeService.changePassword(
            passwordResetDTO.getNewPassword(), passwordResetDTO.getEmail());
    if (Objects.nonNull(employee)) {
      return ResponseDTO.wrapSuccessResult(employee,"CHANGE PASSWORD SUCCESSFUL");
    }
    return Helper.failedResponse("CHANGE PASSWORD FAILED");
  }

  @PutMapping(value = "/api/changeActiveState/{employeeId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
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
      ResponseDTO response = new ResponseDTO("STATUS CHANGE SUCCESSFUL", Constants.SUCCESS, result);
      return ResponseEntity.ok(response);
    }
    return Helper.failedResponse("STATUS UPDATE FAILED");
  }

  @PutMapping(value = "/api/selfChangePassword")
  public ResponseEntity<?> selfChangePassword(
          @RequestBody ChangePasswordDTO changePasswordDTO, Authentication authentication) {
    if (authentication == null) return Helper.failedResponse("Auth token required");
    Employee user = employeeService.findEmployeeByEmail(authentication.getName());

    boolean isPasswordValid =
        CommonHelper.MatchBCryptPassword(user.getPassword(), changePasswordDTO.getOldPassword());

    if (isPasswordValid && user.getEnabled()) {
      String encodedNewPassword = bCryptPasswordEncoder.encode(changePasswordDTO.getNewPassword());
      user.setPassword(encodedNewPassword);
      Employee e = employeeRepository.save(user);
      return ResponseDTO.wrapSuccessResult(e, "PASSWORD CHANGE SUCCESSFUL");
    }
    return Helper.failedResponse("PASSWORD INVALID OR USER DISABLED");
  }
}
