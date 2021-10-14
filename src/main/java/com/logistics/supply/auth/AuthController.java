package com.logistics.supply.auth;

import com.logistics.supply.dto.JwtResponse;
import com.logistics.supply.dto.LoginRequest;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.VerificationTokenRepository;
import com.logistics.supply.security.PasswordEncoder;
import com.logistics.supply.service.AbstractRestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.buildNewUserEmail;
import static com.logistics.supply.util.Constants.*;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController extends AbstractRestService {

  private final JwtService jwtService;
  private final AuthService authService;
  private final EmployeeRepository employeeRepository;

   final PasswordEncoder passwordEncoder;
   final EmailSender emailSender;
   final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired AuthenticationManager authenticationManager;

  @PostMapping("/admin/signup")
  public ResponseEntity<?> signUp(@RequestBody RegistrationRequest request) {
    try {
      Employee employee = authService.adminRegistration(request);
      ResponseDTO response = new ResponseDTO("SIGNUP_SUCCESSFUL", SUCCESS, employee);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e.getMessage());
    }
    return failedResponse("SIGNUP_FAILED");
  }

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

    if (!authentication.isAuthenticated()) return failedResponse("INVALID_CREDENTIALS");
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtService.generateToken(authentication);

    AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

    List<String> roles =
        userDetails.getAuthorities().stream()
            .map(x -> x.getAuthority())
            .collect(Collectors.toList());

    employeeRepository.updateLastLogin(new Date(), userDetails.getUsername());
    ResponseDTO response =
        new ResponseDTO(
            "LOGIN_SUCCESSFUL", SUCCESS, new JwtResponse(jwt, userDetails.getEmployee(), roles));
    return ResponseEntity.ok(response);
  }


  public ResponseEntity<?> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
