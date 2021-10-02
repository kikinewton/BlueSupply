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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
  public ResponseEntity<?> signUp(@RequestBody @Valid RegistrationRequest request) {
    try {
      Employee employee = authService.adminRegistration(request);
      ResponseDTO response = new ResponseDTO("SIGNUP_SUCCESSFUL", SUCCESS, employee);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return failedResponse("SIGNUP_FAILED");
  }

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
      throws Exception {

    var employee =
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

  //  @PutMapping("/admin/{adminId}/changeEmployeeStatus")
  //  public ResponseDTO<Object> enableOrDisableEmployee(
  //      @PathVariable("adminId") int adminId, @RequestBody EmployeeStateDTO employeeStateDTO) {
  //    Employee admin = employeeService.findEmployeeById(adminId);
  //    if (Objects.isNull(admin))
  //      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), "ADMIN DOES NOT EXIST", ERROR);
  //    boolean isAdmin = employeeService.verifyEmployeeRole(adminId, EmployeeRole.ROLE_ADMIN);
  //
  //    Employee employee = employeeService.findEmployeeById(employeeStateDTO.getEmployeeId());
  //    if (Objects.isNull(employee))
  //      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), "EMPLOYEE DOES NOT EXIST", ERROR);
  //
  //    boolean isEmployeeAdmin =
  //        employeeService.verifyEmployeeRole(
  //            employeeStateDTO.getEmployeeId(), EmployeeRole.ROLE_ADMIN);
  //    if (isAdmin && !isEmployeeAdmin) {
  //      employee.setEnabled(employeeStateDTO.isChangeState());
  //      try {
  //        Employee emp = employeeRepository.save(employee);
  //        return new ResponseDTO<>(HttpStatus.OK.name(), "STATUS CHANGED", SUCCESS);
  //      } catch (Exception e) {
  //        log.error(e.getMessage());
  //      }
  //    }
  //    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  //  }

  //  @PutMapping(value = "/admin/{adminId}/changePassword")
  //  public ResponseDTO<Object> changePassword(
  //      @PathVariable("adminId") int adminId, @RequestBody ChangePasswordDTO changePasswordDTO) {
  //    Employee admin = employeeService.findEmployeeById(adminId);
  //    if (Objects.isNull(admin))
  //      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), "ADMIN DOES NOT EXIST", ERROR);
  //    boolean isAdmin = employeeService.verifyEmployeeRole(adminId, EmployeeRole.ROLE_ADMIN);
  //
  //    Employee employee = employeeService.findEmployeeById(changePasswordDTO.getEmployeeId());
  //    if (Objects.isNull(employee))
  //      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), "EMPLOYEE DOES NOT EXIST", ERROR);
  //
  //    boolean isPasswordValid =
  //        MatchBCryptPassword(employee.getPassword(), changePasswordDTO.getOldPassword());
  //    System.out.println("Password is valid: " + isPasswordValid);
  //
  //    if (isPasswordValid
  //        && changePasswordDTO.getNewPassword().length() > 5
  //        && employee.getEnabled()) {
  //      System.out.println("Password is valid and new password has length greater than 5");
  //      String encodedNewPassword =
  // bCryptPasswordEncoder.encode(changePasswordDTO.getNewPassword());
  //      employee.setPassword(encodedNewPassword);
  //      Employee emp = employeeRepository.save(employee);
  //      if (Objects.nonNull(emp)) {
  //        String emailContent =
  //            buildNewUserEmail(
  //                emp.getLastName().toUpperCase(Locale.ROOT),
  //                "",
  //                EmailType.NEW_USER_PASSWORD_MAIL.name(),
  //                NEW_USER_PASSWORD_MAIL,
  //                changePasswordDTO.getNewPassword());
  //        try {
  //          emailSender.sendMail(
  //              employee.getEmail(), EmailType.NEW_USER_CONFIRMATION_MAIL, emailContent);
  //        } catch (Exception e) {
  //          log.error(e.getMessage());
  //        }
  //        return new ResponseDTO<>(HttpStatus.OK.name(), "PASSWORD CHANGED", SUCCESS);
  //      }
  //    }
  //    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  //  }

  public ResponseEntity<?> failedResponse(String message) {
    ResponseDTO failed = new ResponseDTO(message, ERROR, null);
    return ResponseEntity.badRequest().body(failed);
  }
}
