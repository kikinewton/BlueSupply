package com.logistics.supply.fixture;

import com.logistics.supply.dto.RequestCategoryDto;

public class RequestCategoryFixtureDto {

     RequestCategoryFixtureDto() {
    }

    public static RequestCategoryDto getRequestCategory() {
        RequestCategoryDto requestCategoryDto = new RequestCategoryDto();
        requestCategoryDto.setDescription("Water");
        requestCategoryDto.setName("Water");
        return requestCategoryDto;
    }
}
