package com.logistics.supply.dto;

import lombok.Getter;

import javax.validation.constraints.Size;
import java.util.Set;

@Getter
public class BulkFloatsDTO {
    @Size(min = 1)
    Set<FloatDto> floats;
}
