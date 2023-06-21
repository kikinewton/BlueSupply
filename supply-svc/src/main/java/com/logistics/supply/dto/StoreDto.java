package com.logistics.supply.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class StoreDto {

    @NotBlank(message = "Provide store name")
    private String name;
}
