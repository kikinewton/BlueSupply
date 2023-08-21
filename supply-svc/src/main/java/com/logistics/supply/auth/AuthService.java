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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final EmployeeService employeeService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;

    public Employee adminRegistration(@Valid RegistrationRequest request) {

        log.info("Sign up user with email: {}", request.getEmail());
        return employeeService.signUp(request);
    }

    public JwtResponse authenticate(
            LoginRequest loginRequest,
            HttpServletRequest request) {

        log.info("Login employee with email: {}", loginRequest.getEmail());
        Employee employee = employeeService.findEmployeeByEmail(loginRequest.getEmail());

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(), loginRequest.getPassword()));

        String jwt = jwtService.generateToken(authentication);
        List<String> roles =
                employee.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        return new JwtResponse(jwt, "refresh", employee, roles);

    }

    private void authenticateUser(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    public Employee changePassword(ChangePasswordDto changePasswordDto, String email) {

        log.info("Change password for employee with email: {}", email);
        Employee employee = employeeService.findEmployeeByEmail(email);
        if (!employee.isEnabled()) {
            throw new EmployeeNotEnabledException(email);
        }
        CommonHelper.matchBCryptPassword(
                employee.getPassword(),
                changePasswordDto.getOldPassword());
        return employeeService.selfPasswordChange(employee, changePasswordDto);
    }
}
