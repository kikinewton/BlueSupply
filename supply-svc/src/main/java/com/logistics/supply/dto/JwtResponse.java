package com.logistics.supply.dto;

import com.logistics.supply.model.Employee;
import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {

  private String token;
  private String refreshToken;
  private String type = "Bearer";
  private Employee employee;
  private List<String> roles;

  public JwtResponse(String accessToken, String refreshToken, Employee employee, List<String> roles) {
    this.token = accessToken;
    this.refreshToken = refreshToken;
    this.employee = employee;
    this.roles = roles;
  }
}
