package com.logistics.supply.auth;

import com.logistics.supply.dto.*;
import com.logistics.supply.model.Employee;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "Endpoint to signup new employees", tags = "AUTH")
  @PostMapping("/admin/signup")
  public ResponseEntity<ResponseDto<Employee>> signUp(@Valid @RequestBody RegistrationRequest request) {

      Employee employee = authService.adminRegistration(request);
      return ResponseDto.wrapSuccessResult( employee, "SIGNUP SUCCESSFUL");
  }

  @Operation(summary = "Endpoint for login", tags = "AUTH")
  @PostMapping("/login")
  public ResponseEntity<ResponseDto<JwtResponse>> authenticateUser(
          @Valid @RequestBody LoginRequest loginRequest) throws InvalidCredentialsException {

    JwtResponse jwtResponse = authService.authenticate(loginRequest);
    return ResponseDto.wrapSuccessResult(jwtResponse, "LOGIN SUCCESSFUL");
  }

  @Operation(summary = "Change password for login user", tags = "AUTH")
  @PutMapping(value = "/changePassword")
  public ResponseEntity<ResponseDto<Employee>> changePassword(
      Authentication authentication,
      @Valid @RequestBody ChangePasswordDto changePasswordDTO)  {

    String email = authentication.getName();
    Employee employee = authService.changePassword(changePasswordDTO, email);
    return ResponseDto.wrapSuccessResult(employee, "PASSWORD CHANGE SUCCESSFUL");

  }
}
