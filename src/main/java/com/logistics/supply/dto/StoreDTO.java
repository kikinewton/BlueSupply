package com.logistics.supply.dto;

import com.logistics.supply.model.Department;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StoreDTO {
    private String name;
    private Department department;
}
