package com.logistics.supply.auth;

import com.logistics.supply.dto.EmployeeDTO;
import com.logistics.supply.dto.LoginRequest;
import com.logistics.supply.dto.RegistrationRequest;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.email.EmailSender;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.VerificationToken;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.VerificationTokenRepository;
import com.logistics.supply.security.PasswordEncoder;
import com.logistics.supply.service.AbstractRestService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

  @PostMapping("/self/signup")
  public ResponseDTO<Employee> selfSignUp(@RequestBody EmployeeDTO employeeDTO) {
    try {
      Employee employee = authService.register(employeeDTO);
      if (Objects.nonNull(employee)) {
        String token = authService.generateVerificationToken(employee);
        String link = BASE_URL + "/api/auth/accountVerification/" + token;
        String emailContent =
            buildEmail(
                employee.getLastName().toUpperCase(Locale.ROOT),
                link,
                EmailType.NEW_USER_CONFIRMATION_MAIL.name(),
                NEW_EMPLOYEE_CONFIRMATION_MAIL);
        emailSender.sendMail(
            "", employee.getEmail(), EmailType.NEW_USER_CONFIRMATION_MAIL, emailContent);
      }
      return new ResponseDTO<>(HttpStatus.CREATED.name(), employee, SUCCESS);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ResponseDTO<>(ERROR, null, HttpStatus.NOT_FOUND.name());
  }

  @PostMapping("/admin/signup")
  public ResponseDTO<Employee> signUp(@RequestBody RegistrationRequest request) {
    try {
      Employee employee = authService.adminRegistration(request);
            if (Objects.nonNull(employee)) {
              String token = authService.generateVerificationToken(employee);
              String link = BASE_URL + "/api/auth/accountVerification/" + token;
              String emailContent =
                  buildEmail(
                      employee.getLastName(),
                      link,
                      EmailType.NEW_USER_PASSWORD_MAIL.name(),
                      NEW_USER_PASSWORD_MAIL);
              emailSender.sendMail(
                  "bsupply901@gmail.com", employee.getEmail(), EmailType.NEW_USER_CONFIRMATION_MAIL,
       emailContent);
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
  public ResponseDTO<Object> login(@RequestBody LoginRequest loginRequest) {
    String[] nullValues = getNullPropertyNames(loginRequest);
    Set<String> l = Set.of(nullValues);
    boolean isEmailValid = isValidEmailAddress(loginRequest.getEmail());

    if (!isEmailValid) {
      return new ResponseDTO<>(ERROR, null, "INVALID USERNAME OR PASSWORD");
    }

    if (l.size() > 0) {
      return new ResponseDTO<>(ERROR, null, "MISSING REQUIRED LOGIN FIELD");
    }

    Optional<Employee> employee = employeeRepository.findByEmail(loginRequest.getEmail());

    log.info("Employee Present: " + employee.isPresent());

    if (!employee.isPresent())
      return new ResponseDTO<>(ERROR, "INVALID USERNAME OR PASSWORD", HttpStatus.NOT_FOUND.name());
    log.info("EMployee ENABLED: " + employee.get().getEnabled());
    if (!employee.get().getEnabled())
      return new ResponseDTO<>(ERROR, "USER ACCOUNT NOT ENABLED", HttpStatus.UNAUTHORIZED.name());

    String encodedPassword = employee.get().getPassword();
    Map<String, Object> data = new HashMap<>();
    if (MatchBCryptPassword(encodedPassword, loginRequest.getPassword())) {
      String jwtToken = jwtService.generateToken(employee.get());
      data.put("employee", employee.get());
      data.put("token", jwtToken);
      return new ResponseDTO<>(SUCCESS, data, HttpStatus.CREATED.name());
    }
    return new ResponseDTO<>(ERROR, "LOGIN ATTEMPT FAILED", HttpStatus.BAD_REQUEST.name());
  }
}
