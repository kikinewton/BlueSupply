package com.logistics.supply.dto;

import lombok.Getter;
import lombok.ToString;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@ToString
public class EmployeeStateDTO {
    private Integer employeeId;
    private boolean changeState;
}
