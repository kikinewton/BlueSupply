package com.logistics.supply.fixture;

import com.logistics.supply.dto.RequestCategoryDto;

public class RequestCategoryFixtureDto {

    RequestCategoryFixtureDto() {
    }

    public static RequestCategoryDto getRequestCategory() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name = "Water";
        private String description = "Water";

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public RequestCategoryDto build() {
            RequestCategoryDto requestCategoryDto = new RequestCategoryDto();
            requestCategoryDto.setName(name);
            requestCategoryDto.setDescription(description);
            return requestCategoryDto;
        }
    }
}
