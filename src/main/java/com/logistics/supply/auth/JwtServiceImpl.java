//package com.logistics.supply.auth;
//
//import com.logistics.supply.model.Employee;
//import com.logistics.supply.service.EmployeeService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import io.jsonwebtoken.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//
//import javax.xml.bind.DatatypeConverter;
//import java.time.Instant;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//@Data
//@Service
//@Slf4j
//public class JwtServiceImpl implements JwtService {
//
//  private final JwtConfig jwtConfig;
//  private final EmployeeService employeeService;
//
//  public String generateToken(Authentication authentication) {
//
////    AppUserDetails userPrincipal = (AppUserDetails) authentication.getPrincipal();
//    String email = authentication.getName();
//    return Jwts.builder()
//        .setSubject(email)
//        .setIssuedAt(new Date())
//        .setExpiration(new Date((new Date()).getTime() + jwtConfig.getValidityInSeconds()))
//        .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecretKey())
//        .compact();
//  }
//
//
//
//  public String getUserNameFromJwtToken(String token) {
//    return Jwts.parser()
//        .setSigningKey(jwtConfig.getSecretKey())
//        .parseClaimsJws(token)
//        .getBody()
//        .getSubject();
//  }
//
//  @Override
//  public boolean validateToken(String authToken) {
//    try {
//      Jwts.parser().setSigningKey(jwtConfig.getSecretKey()).parseClaimsJws(authToken);
//      return true;
//    } catch (SignatureException e) {
//      log.error("Invalid JWT signature: {}", e.getMessage());
//    } catch (MalformedJwtException e) {
//      log.error("Invalid JWT token: {}", e.getMessage());
//    } catch (ExpiredJwtException e) {
//      log.error("JWT token is expired: {}", e.getMessage());
//    } catch (UnsupportedJwtException e) {
//      log.error("JWT token is unsupported: {}", e.getMessage());
//    } catch (IllegalArgumentException e) {
//      log.error("JWT claims string is empty: {}", e.getMessage());
//    }
//
//    return false;
//  }
//
//
//}
