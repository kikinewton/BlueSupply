package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
public class ChangePasswordDto {
    @NotBlank
    private String oldPassword;

    @ValidPassword
    private String newPassword;
}
