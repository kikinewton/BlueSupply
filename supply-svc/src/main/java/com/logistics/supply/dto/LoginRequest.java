package com.logistics.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

  @Email(message = "Email is invalid")
  private String email;

  @NotBlank(message = "Provide password")
  private String password;

}
