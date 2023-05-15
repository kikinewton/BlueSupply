package com.logistics.supply.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class EmployeeStateDTO {
    private Integer employeeId;
    private boolean changeState;
}
