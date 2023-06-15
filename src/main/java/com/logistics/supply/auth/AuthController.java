package com.logistics.supply.auth;

import com.logistics.supply.dto.*;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.util.CommonHelper;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.MatchBCryptPassword;
import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final JwtService jwtService;

  private final AuthService authService;

  private final EmployeeRepository employeeRepository;

  @Autowired AuthenticationManager authenticationManager;

  @Autowired
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Operation(summary = "Endpoint to signup new employees", tags = "AUTH")
  @PostMapping("/admin/signup")
  public ResponseEntity<?> signUp(@RequestBody RegistrationRequest request) {
    try {
      boolean isEmailValid = CommonHelper.isValidEmailAddress(request.getEmail());
      if (!isEmailValid) {
        throw new GeneralException("EMAIL INVALID", HttpStatus.BAD_REQUEST);
      }
      boolean employeeExist = employeeRepository.findByEmail(request.getEmail()).isPresent();
      if (employeeExist) {
        return failedResponse("EMPLOYEE WITH EMAIL ALREADY EXIST");
      }
      Employee employee = authService.adminRegistration(request);
      ResponseDTO response = new ResponseDTO("SIGNUP SUCCESSFUL", SUCCESS, employee);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("SIGNUP FAILED");
  }

  @Operation(summary = "Endpoint for login", tags = "AUTH")
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
      throws Exception {

    Optional<Employee> employee =
        employeeRepository.findByEmailAndEnabledIsTrue(loginRequest.getEmail());
    if (!employee.isPresent()) throw new GeneralException("USER INVALID", HttpStatus.BAD_REQUEST);
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword()));

    if (!authentication.isAuthenticated())
      throw new GeneralException("INVALID USERNAME OR PASSWORD", HttpStatus.BAD_REQUEST);

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtService.generateToken(authentication);

    Employee userDetails =
        employeeRepository
            .findByEmailAndEnabledIsTrue(authentication.getName())
            .orElseThrow(() -> new GeneralException("INVALID USER", HttpStatus.FORBIDDEN));

    List<String> roles =
        userDetails.getRoles().stream().map(x -> x.getName()).collect(Collectors.toList());

    CompletableFuture.runAsync(
        () -> employeeRepository.updateLastLogin(new Date(), userDetails.getEmail()));

    ResponseDTO response =
        new ResponseDTO(
            "LOGIN SUCCESSFUL", SUCCESS, new JwtResponse(jwt, "refreshToken", userDetails, roles));
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Change password for login user", tags = "AUTH")
  @PutMapping(value = "/changePassword")
  public ResponseEntity<?> changePassword(
      Authentication authentication, @Valid @RequestBody ChangePasswordDTO changePasswordDTO) throws GeneralException {
    Optional<Employee> employee =
        employeeRepository.findByEmailAndEnabledIsTrue(authentication.getName());
    if (!employee.isPresent()) {
      throw new GeneralException("USER NOT FOUND", HttpStatus.NOT_FOUND);
    }
    boolean isPasswordValid =
        MatchBCryptPassword(employee.get().getPassword(), changePasswordDTO.getOldPassword());
    if (isPasswordValid && employee.get().getEnabled()) {
      String encodedNewPassword = bCryptPasswordEncoder.encode(changePasswordDTO.getNewPassword());
      employee.get().setPassword(encodedNewPassword);
      Employee emp = employeeRepository.save(employee.get());
      if (Objects.nonNull(emp)) {
        // fixme send email to inform change password successful
        ResponseDTO response = new ResponseDTO("PASSWORD CHANGE SUCCESSFUL", SUCCESS, emp);
        return ResponseEntity.ok(response);
      }
    }
    return failedResponse("CHANGE PASSWORD FAILED");
  }
}
