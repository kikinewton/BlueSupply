package com.logistics.supply.dto;

import lombok.Getter;

import javax.validation.constraints.Size;

@Getter
public class ChangePasswordDTO {
    private String oldPassword;
    @Size(min = 7, max = 30)
    private String newPassword;
}
