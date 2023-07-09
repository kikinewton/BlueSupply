package com.logistics.supply.auth;

import com.logistics.supply.dto.ChangePasswordDto;
import com.logistics.supply.dto.JwtResponse;
import com.logistics.supply.dto.LoginRequest;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.exception.EmployeeNotEnabledException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Role;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.util.CommonHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService  {

  @Autowired
  AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final EmployeeService employeeService;

  public Employee adminRegistration(@Valid RegistrationRequest request) {

    log.info("Sign up user with email: {}", request.getEmail());
    return employeeService.signUp(request);
  }

  public JwtResponse authenticate(LoginRequest loginRequest) throws InvalidCredentialsException {

    log.info("Login employee with email: {}", loginRequest.getEmail());
    Employee employee = employeeService.findEmployeeByEmail(loginRequest.getEmail());
    Authentication authentication =
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            employee.getEmail(), loginRequest.getPassword()));
    if (!authentication.isAuthenticated()) {
      throw new InvalidCredentialsException("Invalid username or password");
    }
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtService.generateToken(authentication);
    List<String> roles =
            employee.getRoles().stream().map(Role::getName).collect(Collectors.toList());

    return new JwtResponse(jwt, "refresh", employee, roles);
  }

  public Employee changePassword(ChangePasswordDto changePasswordDto, String email) {

    log.info("Change password for employee with email: {}", email);
    Employee employee = employeeService.findEmployeeByEmail(email);
    if (!employee.isEnabled()) {
      throw new EmployeeNotEnabledException(email);
    }
      CommonHelper.matchBCryptPassword(employee.getPassword(), changePasswordDto.getOldPassword());
    return employeeService.selfPasswordChange(employee, changePasswordDto);
  }
}
