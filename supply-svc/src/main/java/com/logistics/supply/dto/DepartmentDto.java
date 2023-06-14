package com.logistics.supply.dto;

import com.logistics.supply.model.Department;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {

    @ValidName
    private String name;
    @ValidDescription
    private String description;
    public static final DepartmentDto toDto(Department department) {
        DepartmentDto departmentDTO = new DepartmentDto();
        BeanUtils.copyProperties(department, departmentDTO);
        return departmentDTO;
    }
}
