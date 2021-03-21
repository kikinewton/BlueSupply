package com.logistics.supply.dto;

import lombok.Getter;

@Getter
public class ChangePasswordDTO {
    private Integer employeeId;
    private String oldPassword;
    private String newPassword;
}
