package com.logistics.supply.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
public class ChangePasswordDTO {
    @NotBlank
    private String oldPassword;
    @NotBlank
    @Size(min = 7, max = 30)
    private String newPassword;
}
