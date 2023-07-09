package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class PasswordResetDto {

    @NotBlank
    private String token;

    @ValidPassword(message = "Password must not be blank")
    private String newPassword;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email must not be blank")
    private String email;
}
