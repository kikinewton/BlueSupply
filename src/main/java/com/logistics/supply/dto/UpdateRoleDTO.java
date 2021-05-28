package com.logistics.supply.dto;

import com.logistics.supply.model.EmployeeRole;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleDTO {
    int employeeId;
    List<EmployeeRole> roles;
}
