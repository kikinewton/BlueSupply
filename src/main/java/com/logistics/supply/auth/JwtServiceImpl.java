package com.logistics.supply.auth;

import com.logistics.supply.model.Employee;
import com.logistics.supply.service.EmployeeService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

  private final JwtConfig jwtConfig;
  private final EmployeeService employeeService;

  @Override
  public String generateToken(Employee employee) {
    Date currentDate = new Date();
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
    Map<String, Object> claims = new HashMap<>();
    claims.put("employeeId", employee.getId());

    return Jwts.builder()
        .setId(String.valueOf(employee.getId()))
        .setIssuedAt(currentDate)
        .setSubject("Employee")
        .setIssuer(jwtConfig.getIssuer())
        .setExpiration(Date.from(Instant.now().plusSeconds(jwtConfig.getValidityInSeconds())))
        .signWith(signatureAlgorithm, jwtConfig.getSecretKey())
        .setClaims(claims)
        .compact();
  }

  @Override
  public String validateToken(String token) {
    if (token.indexOf("Bearer") != 0) {
      return null;
    }

    String tokenStr = token.substring(6);

    try {

      Claims claims =
          Jwts.parser()
              .setSigningKey(DatatypeConverter.parseBase64Binary(jwtConfig.getSecretKey()))
              .parseClaimsJws(tokenStr)
              .getBody();
      String userIdStr = String.valueOf(claims.get("employeeId"));
      Employee employee = employeeService.findEmployeeById(Integer.valueOf(userIdStr));
      if (!employee.getEnabled()) {
        throw new Exception("Employee Not Active! Please contact yor admin");
      }
      return userIdStr;
    } catch (MalformedJwtException ex) {
      log.error("malformed token");
      return null;
    } catch (ExpiredJwtException ex) {
      log.error("Expired JWT token");
      return null;
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported Access Token");
      return null;
    } catch (IllegalArgumentException ex) {
      log.error("JWT claims string is empty.");
      return null;
    } catch (Exception e) {
      log.error("Invalid Token");
      return null;
    }
  }
}
