package com.logistics.supply.dto;

import com.logistics.supply.model.EmployeeRole;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateRoleDTO {
    private int employeeId;
    private List<EmployeeRole> roles;
}
