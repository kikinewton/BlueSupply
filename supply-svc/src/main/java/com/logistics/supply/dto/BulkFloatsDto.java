package com.logistics.supply.dto;

import lombok.Getter;

import jakarta.validation.constraints.Size;
import java.util.Set;

@Getter
public class BulkFloatsDto {
    @Size(min = 1)
    Set<FloatDto> floats;
}
