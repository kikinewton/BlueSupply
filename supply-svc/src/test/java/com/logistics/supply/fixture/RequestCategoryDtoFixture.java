package com.logistics.supply.fixture;

import com.logistics.supply.dto.RequestCategoryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

public class RequestCategoryDtoFixture {

    private RequestCategoryDtoFixture() {}

    public static RequestCategoryDtoBuilder withDefaults() {
        return new RequestCategoryDtoBuilder();
    }

    @Getter
    @With
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestCategoryDtoBuilder {

        private String name        = "Water";
        private String description = "Water";

        public RequestCategoryDto build() {
            RequestCategoryDto dto = new RequestCategoryDto();
            dto.setName(name);
            dto.setDescription(description);
            return dto;
        }
    }
}
