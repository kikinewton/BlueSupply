//package com.logistics.supply.auth;
//
//import com.logistics.supply.repository.EmployeeRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Date;
//
//public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
//
//  @Autowired private EmployeeRepository employeeRepository;
//
//  @Override
//  public void onAuthenticationSuccess(
//      HttpServletRequest httpServletRequest,
//      HttpServletResponse httpServletResponse,
//      Authentication authentication)
//      throws IOException, ServletException {
//    employeeRepository.updateLastLogin(new Date());
//  }
//}
