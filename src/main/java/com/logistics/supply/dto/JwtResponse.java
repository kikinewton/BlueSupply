package com.logistics.supply.dto;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {

  private String token;
  private String type = "Bearer";
  private String email;
  private List<String> roles;

  public JwtResponse(String accessToken, String email, List<String> roles) {
    this.token = accessToken;
    this.email = email;
    this.roles = roles;
  }
}
