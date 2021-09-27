package com.logistics.supply.dto;

import com.logistics.supply.model.Employee;
import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {

  private String token;
  private String type = "Bearer";
  private Employee employee;
  private List<String> roles;

  public JwtResponse(String accessToken, Employee employee, List<String> roles) {
    this.token = accessToken;
    this.employee = employee;
    this.roles = roles;
  }
}
