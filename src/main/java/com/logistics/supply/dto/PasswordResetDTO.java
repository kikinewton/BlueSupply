package com.logistics.supply.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.annotation.ValidPassword;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
public class PasswordResetDTO {
    @NotBlank
    private String token;

    @ValidPassword
    @JsonIgnore
    private String newPassword;

    @Email
    private String email;
}
