package com.logistics.supply.auth;

import com.logistics.supply.dto.*;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.VerificationToken;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.VerificationTokenRepository;
import com.logistics.supply.security.PasswordEncoder;
import com.logistics.supply.service.AbstractRestService;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.CommonHelper.*;
import static com.logistics.supply.util.Constants.*;

@RestController
@Data
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController extends AbstractRestService {

  private final JwtService jwtService;
  private final AuthService authService;
  private final EmployeeRepository employeeRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailSender emailSender;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired AuthenticationManager authenticationManager;

  //  @PostMapping("/self/signup")
  //  public ResponseDTO<Employee> selfSignUp(@RequestBody EmployeeDTO employeeDTO) {
  //    try {
  //      Employee employee = authService.register(employeeDTO);
  //      if (Objects.nonNull(employee)) {
  //        String token = authService.generateVerificationToken(employee);
  //        String link = BASE_URL + "/api/auth/accountVerification/" + token;
  //        String emailContent =
  //            buildEmail(
  //                employee.getLastName().toUpperCase(Locale.ROOT),
  //                link,
  //                EmailType.NEW_USER_CONFIRMATION_MAIL.name(),
  //                NEW_EMPLOYEE_CONFIRMATION_MAIL);
  //        emailSender.sendMail(
  //            "", employee.getEmail(), EmailType.NEW_USER_CONFIRMATION_MAIL, emailContent);
  //      }
  //      return new ResponseDTO<>(HttpStatus.CREATED.name(), employee, SUCCESS);
  //    } catch (Exception e) {
  //      log.error(e.getMessage());
  //    }
  //    return new ResponseDTO<>(ERROR, null, HttpStatus.NOT_FOUND.name());
  //  }

  @PostMapping("/admin/signup")
  public ResponseDTO<Employee> signUp(@RequestBody RegistrationRequest request) {
    try {
      Employee employee = authService.adminRegistration(request);
      if (Objects.nonNull(employee)) {
        //              String token = authService.generateVerificationToken(employee);
        //              String link = BASE_URL + "/api/auth/accountVerification/" + token;
        String emailContent =
            buildNewUserEmail(
                employee.getLastName().toUpperCase(Locale.ROOT),
                "",
                EmailType.NEW_USER_PASSWORD_MAIL.name(),
                NEW_USER_PASSWORD_MAIL,
                "password1.com");
        emailSender.sendMail(
            employee.getEmail(), EmailType.NEW_USER_CONFIRMATION_MAIL, emailContent);
      }
      return new ResponseDTO<>(HttpStatus.CREATED.name(), employee, SUCCESS);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ResponseDTO<>(ERROR, null, HttpStatus.NOT_FOUND.name());
  }

  @GetMapping(value = "accountVerification/{token}")
  public ResponseDTO verifyAccount(@PathVariable String token) {
    Optional<VerificationToken> newToken = verificationTokenRepository.findByToken(token);
    if (newToken.isPresent())
      return new ResponseDTO(
          ERROR, "Employee account has already been activated", HttpStatus.CONFLICT.name());
    try {
      authService.verifyAccount(token);
      log.info("Account Activated Successfully");
      return new ResponseDTO(SUCCESS, "ACCOUNT ACTIVATED", HttpStatus.OK.name());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return new ResponseDTO(ERROR, null, HttpStatus.NOT_FOUND.name());
  }

  @PostMapping("/login")
  public ResponseDTO<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword()));

    System.out.println("======>>>>>" + authentication.toString());
//
    if (!authentication.isAuthenticated())
      return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, "INVALID USERNAME OR PASSWORD");
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtService.generateToken(authentication);

    AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

    List<String> roles =
        userDetails.getAuthorities().stream()
            .map(x -> x.getAuthority())
            .collect(Collectors.toList());

    return new ResponseDTO<>(
        SUCCESS, new JwtResponse(jwt, userDetails.getUsername(), roles), HttpStatus.OK.name());
  }

  //  @PostMapping("/login")
  //  public ResponseDTO<Object> login(@RequestBody LoginRequest loginRequest) {
  //    String[] nullValues = getNullPropertyNames(loginRequest);
  //    Set<String> l = new HashSet<>(Arrays.asList(nullValues));
  //    boolean isEmailValid = isValidEmailAddress(loginRequest.getEmail());
  //
  //    if (!isEmailValid) {
  //      return new ResponseDTO<>(ERROR, null, "INVALID USERNAME OR PASSWORD");
  //    }
  //
  //    if (l.size() > 0) {
  //      return new ResponseDTO<>(ERROR, null, "MISSING REQUIRED LOGIN FIELD");
  //    }
  //
  //    Optional<Employee> employee = employeeRepository.findByEmail(loginRequest.getEmail());
  //
  //    log.info("Employee Present: " + employee.isPresent());
  //
  //    if (!employee.isPresent())
  //      return new ResponseDTO<>(HttpStatus.NOT_FOUND.name(), null, "INVALID USERNAME OR
  // PASSWORD");
  //    log.info("EMPLOYEE ENABLED: " + employee.get().getEnabled());
  //    if (!employee.get().getEnabled())
  //      return new ResponseDTO<>(ERROR, "USER ACCOUNT NOT ENABLED",
  // HttpStatus.UNAUTHORIZED.name());
  //
  //    String encodedPassword = employee.get().getPassword();
  //    Map<String, Object> data = new HashMap<>();
  //    if (MatchBCryptPassword(encodedPassword, loginRequest.getPassword())) {
  //      String jwtToken = jwtService.generateToken(employee.get());
  //      data.put("employee", employee.get());
  //      data.put("token", jwtToken);
  //      return new ResponseDTO<>(SUCCESS, data, HttpStatus.CREATED.name());
  //    }
  //    return new ResponseDTO<>(ERROR, "LOGIN ATTEMPT FAILED", HttpStatus.BAD_REQUEST.name());
  //  }

  @PutMapping("/admin/{adminId}/changeEmployeeStatus")
  public ResponseDTO<Object> enableOrDisableEmployee(
      @PathVariable("adminId") int adminId, @RequestBody EmployeeStateDTO employeeStateDTO) {
    Employee admin = employeeService.findEmployeeById(adminId);
    if (Objects.isNull(admin))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), "ADMIN DOES NOT EXIST", ERROR);
    boolean isAdmin = employeeService.verifyEmployeeRole(adminId, EmployeeLevel.ADMIN);

    Employee employee = employeeService.findEmployeeById(employeeStateDTO.getEmployeeId());
    if (Objects.isNull(employee))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), "EMPLOYEE DOES NOT EXIST", ERROR);

    boolean isEmployeeAdmin =
        employeeService.verifyEmployeeRole(
            employeeStateDTO.getEmployeeId(), EmployeeLevel.ADMIN);
    if (isAdmin && !isEmployeeAdmin) {
      employee.setEnabled(employeeStateDTO.isChangeState());
      try {
        Employee emp = employeeRepository.save(employee);
        return new ResponseDTO<>(HttpStatus.OK.name(), "STATUS CHANGED", SUCCESS);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }

  @PutMapping(value = "/admin/{adminId}/changePassword")
  public ResponseDTO<Object> changePassword(
      @PathVariable("adminId") int adminId, @RequestBody ChangePasswordDTO changePasswordDTO) {
    Employee admin = employeeService.findEmployeeById(adminId);
    if (Objects.isNull(admin))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), "ADMIN DOES NOT EXIST", ERROR);
    boolean isAdmin = employeeService.verifyEmployeeRole(adminId, EmployeeLevel.ADMIN);

    Employee employee = employeeService.findEmployeeById(changePasswordDTO.getEmployeeId());
    if (Objects.isNull(employee))
      return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), "EMPLOYEE DOES NOT EXIST", ERROR);

    boolean isPasswordValid =
        MatchBCryptPassword(employee.getPassword(), changePasswordDTO.getOldPassword());
    System.out.println("Password is valid: " + isPasswordValid);

    if (isPasswordValid
        && changePasswordDTO.getNewPassword().length() > 5
        && employee.getEnabled()) {
      System.out.println("Password is valid and new password has length greater than 5");
      String encodedNewPassword = bCryptPasswordEncoder.encode(changePasswordDTO.getNewPassword());
      employee.setPassword(encodedNewPassword);
      Employee emp = employeeRepository.save(employee);
      if (Objects.nonNull(emp)) {
        String emailContent =
            buildNewUserEmail(
                emp.getLastName().toUpperCase(Locale.ROOT),
                "",
                EmailType.NEW_USER_PASSWORD_MAIL.name(),
                NEW_USER_PASSWORD_MAIL,
                changePasswordDTO.getNewPassword());
        try {
          emailSender.sendMail(
              employee.getEmail(), EmailType.NEW_USER_CONFIRMATION_MAIL, emailContent);
        } catch (Exception e) {
          log.error(e.getMessage());
        }
        return new ResponseDTO<>(HttpStatus.OK.name(), "PASSWORD CHANGED", SUCCESS);
      }
    }
    return new ResponseDTO<>(HttpStatus.BAD_REQUEST.name(), null, ERROR);
  }
}
