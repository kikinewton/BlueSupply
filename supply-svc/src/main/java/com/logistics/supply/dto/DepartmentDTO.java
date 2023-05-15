package com.logistics.supply.dto;

import com.logistics.supply.model.Department;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;

@Getter
@Setter
@NoArgsConstructor
public class DepartmentDTO {

    @ValidName
    private String name;
    @ValidDescription
    private String description;
    public static final DepartmentDTO toDto(Department department) {
        DepartmentDTO departmentDTO = new DepartmentDTO();
        BeanUtils.copyProperties(department, departmentDTO);
        return departmentDTO;
    }

}
