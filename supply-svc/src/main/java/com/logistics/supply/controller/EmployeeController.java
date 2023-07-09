package com.logistics.supply.controller;

import com.logistics.supply.dto.ChangePasswordDto;
import com.logistics.supply.dto.EmployeeDto;
import com.logistics.supply.dto.PasswordResetDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.enums.VerificationType;
import com.logistics.supply.exception.EmployeeNotEnabledException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.VerificationTokenService;
import com.logistics.supply.util.CommonHelper;
import com.logistics.supply.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
public class EmployeeController {
  private final EmployeeService employeeService;
  private final VerificationTokenService verificationTokenService;

  @GetMapping("/api/employees")
  public ResponseEntity<ResponseDto<List<Employee>>> getEmployees() {

    List<Employee> employees = employeeService.getAll();
    return ResponseDto.wrapSuccessResult(employees, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping("/api/employees/{employeeId}")
  public ResponseEntity<ResponseDto<Employee>> getEmployeeById(@PathVariable int employeeId) {

    Employee employee = employeeService.getEmployeeById(employeeId);
    return ResponseDto.wrapSuccessResult(employee, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping("/api/employee")
  public ResponseEntity<ResponseDto<Employee>> getEmployeeDetails(Authentication authentication) {

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    return ResponseDto.wrapSuccessResult(employee, Constants.FETCH_SUCCESSFUL);
  }

  @DeleteMapping("/api/employees/{employeeId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Void> deleteEmployee(@PathVariable Integer employeeId) {

    employeeService.deleteById(employeeId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PutMapping(value = "/api/employees/{employeeId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseDto<Employee>> updateEmployee(
      @RequestBody EmployeeDto employeeDto, @PathVariable int employeeId) {

    Employee employee = employeeService.update(employeeId, employeeDto);
    return ResponseDto.wrapSuccessResult(employee, "EMPLOYEE UPDATED");
  }

  @PutMapping(value = "/api/employees/{employeeId}/disable")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseDto<Employee>> disableEmployee(@PathVariable int employeeId) {

    Employee employee = employeeService.disableEmployee(employeeId);
    return ResponseDto.wrapSuccessResult(employee, "DISABLE EMPLOYEE");
  }

  @PutMapping(value = "/resetPassword")
  public ResponseEntity<ResponseDto<String>> changeEmployeePassword(Authentication authentication) {

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    verificationTokenService.generateVerificationToken(
        employee.getEmail(), VerificationType.PASSWORD_RESET);
    return ResponseDto.wrapSuccessResult("", "VERIFICATION CODE SENT TO EMAIL");
  }

  @PutMapping(value = "/api/admin/employees/{employeeId}/resetPassword")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseDto<Boolean>> resetPasswordByAdmin(
      @PathVariable("employeeId") int employeeId) {

    Employee employee = employeeService.findEmployeeById(employeeId);

    verificationTokenService.generateVerificationToken(
        employee.getEmail(), VerificationType.PASSWORD_RESET);

    return ResponseDto.wrapSuccessResult(true, "VERIFICATION CODE SENT TO EMAIL");
  }

  @PostMapping(value = "/resetPasswordConfirmation")
  public ResponseEntity<ResponseDto<Employee>> resetPasswordConfirmation(
      @Valid @RequestBody PasswordResetDto passwordResetDTO) {

    verificationTokenService.checkVerificationCode(
        passwordResetDTO.getToken(), passwordResetDTO.getEmail());

    Employee employee =
        employeeService.changePassword(
            passwordResetDTO.getNewPassword(), passwordResetDTO.getEmail());

    return ResponseDto.wrapSuccessResult(employee, "CHANGE PASSWORD SUCCESSFUL");
  }

  @PutMapping(value = "/api/changeActiveState/{employeeId}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseDto<Employee>> changeEmployeeStatus(
      @PathVariable("employeeId") int employeeId) {

    Employee employee = employeeService.changeEnableStatus(employeeId);
    return ResponseDto.wrapSuccessResult(employee, "STATUS CHANGE SUCCESSFUL");
  }

  @PutMapping(value = "/api/selfChangePassword")
  public ResponseEntity<ResponseDto<Employee>> selfChangePassword(
      Authentication authentication,
      @Valid @RequestBody ChangePasswordDto changePasswordDto) {

    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    if (!employee.isEnabled()) {
      throw new EmployeeNotEnabledException(employee.getEmail());
    }

    CommonHelper.matchBCryptPassword(employee.getPassword(), changePasswordDto.getOldPassword());
    Employee updatedEmployee = employeeService.selfPasswordChange(employee, changePasswordDto);
    return ResponseDto.wrapSuccessResult(updatedEmployee, "PASSWORD CHANGE SUCCESSFUL");
  }
}
