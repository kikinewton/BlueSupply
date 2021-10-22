package com.logistics.supply.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginRequest {
  @Email(message = "Email is invalid")
  private String email;
  @NotBlank(message = "Provide password")
  private String password;
}
