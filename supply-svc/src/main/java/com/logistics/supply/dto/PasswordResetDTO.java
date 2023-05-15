package com.logistics.supply.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import com.logistics.supply.annotation.ValidPassword;

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
