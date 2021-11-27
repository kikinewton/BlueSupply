package com.logistics.supply.auth;

import com.logistics.supply.dto.*;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.security.PasswordEncoder;
import com.logistics.supply.service.RoleService;
import com.logistics.supply.util.CommonHelper;
import com.logistics.supply.util.Helper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.MatchBCryptPassword;
import static com.logistics.supply.util.Constants.ERROR;
import static com.logistics.supply.util.Constants.SUCCESS;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  final PasswordEncoder passwordEncoder;
  final AuthServer authServer;
  private final JwtService jwtService;
  private final AuthService authService;
  private final EmployeeRepository employeeRepository;
  final RoleService roleService;

  @Autowired Helper helper;
  @Autowired AuthenticationManager authenticationManager;
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Operation(summary = "Endpoint to signup new employees", tags = "AUTH")
  @PostMapping("/admin/signup")
  public ResponseEntity<?> signUp(@RequestBody RegistrationRequest request) {
    try {
      boolean isEmailValid = CommonHelper.isValidEmailAddress(request.getEmail());
      if (!isEmailValid) {
        return failedResponse("EMAIL_INVALID");
      }
      boolean employeeExist = employeeRepository.findByEmail(request.getEmail()).isPresent();
      if (employeeExist) {
        return failedResponse("EMPLOYEE_WITH_EMAIL_ALREADY_EXIST");
      }
      Employee employee = authService.adminRegistration(request);
      ResponseDTO response = new ResponseDTO("SIGNUP_SUCCESSFUL", SUCCESS, employee);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e.getMessage());
    }
    return failedResponse("SIGNUP_FAILED");
  }

  @Operation(summary = "Endpoint for login", tags = "AUTH")
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
      throws Exception {

    Optional<Employee> employee =
        employeeRepository.findByEmailAndEnabledIsTrue(loginRequest.getEmail());
    if (!employee.isPresent()) return failedResponse("USER_INVALID");
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword()));

    if (!authentication.isAuthenticated()) {
      ResponseDTO response = new ResponseDTO("INVALID_USERNAME_OR_PASSWORD", ERROR, null);
      return ResponseEntity.badRequest().body(response);
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtService.generateToken(authentication);

    //    AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

    //    List<String> roles =
    //        userDetails.getAuthorities().stream()
    //            .map(x -> x.getAuthority())
    //            .collect(Collectors.toList());

    //      if (!authentication.isAuthenticated()) return failedResponse("INVALID_CREDENTIALS");
    //      SecurityContextHolder.getContext().setAuthentication(authentication);
    //      String authToken = null, refreshToken = "";
    //      String token =
    //          helper.getAccessToken(
    //              loginRequest.getEmail(),
    //              loginRequest.getPassword(),
    //              authServer.getAuthServer(),
    //              authServer.getAuthCode());
    //      if (token.contains(",")) {
    //        String[] tokenResult = token.split(",");
    //        authToken = tokenResult[0];
    //
    //        if (tokenResult.length > 1) refreshToken = tokenResult[1];
    //      }

    Employee userDetails =
        employeeRepository.findByEmailAndEnabledIsTrue(authentication.getName()).get();

    List<String> roles =
        userDetails.getRoles().stream().map(x -> x.getName()).collect(Collectors.toList());

    employeeRepository.updateLastLogin(new Date(), userDetails.getEmail());
    ResponseDTO response =
        new ResponseDTO(
            "LOGIN_SUCCESSFUL", SUCCESS, new JwtResponse(jwt, "refreshToken", userDetails, roles));
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Change password for login user", tags = "AUTH")
  @PutMapping(value = "/changePassword")
  public ResponseEntity<?> changePassword(
      Authentication authentication,@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
    Optional<Employee> employee =
        employeeRepository.findByEmailAndEnabledIsTrue(authentication.getName());
    if (!employee.isPresent()) {
      ResponseDTO response = new ResponseDTO("USER_NOT_FOUND", ERROR, null);
      return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    boolean isPasswordValid =
        MatchBCryptPassword(employee.get().getPassword(), changePasswordDTO.getOldPassword());
    if (isPasswordValid && employee.get().getEnabled()) {
      String encodedNewPassword = bCryptPasswordEncoder.encode(changePasswordDTO.getNewPassword());
      employee.get().setPassword(encodedNewPassword);
      Employee emp = employeeRepository.save(employee.get());
      if (Objects.nonNull(emp)) {
        //fixme send email to inform change password successful
        ResponseDTO response = new ResponseDTO("PASSWORD_CHANGE_SUCCESSFUL", SUCCESS, emp);
        return ResponseEntity.ok(response);
      }
    }

    return failedResponse("CHANGE_PASSWORD_FAILED");
  }

  public ResponseEntity<?> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
