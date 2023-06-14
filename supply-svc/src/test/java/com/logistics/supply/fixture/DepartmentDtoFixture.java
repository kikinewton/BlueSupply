package com.logistics.supply.fixture;

import com.logistics.supply.dto.DepartmentDto;

public class DepartmentDtoFixture {

    DepartmentDtoFixture() {
    }

    public static DepartmentDto getDepartmentDto(
            String name,
            String description) {

        return new DepartmentDto(name, description);
    }
}
