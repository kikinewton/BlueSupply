package com.logistics.supply.dto;

import com.logistics.supply.model.Floats;
import lombok.Getter;

import javax.validation.constraints.Size;
import java.util.Set;

@Getter
public class BulkFloatsDTO {
    @Size(min = 1)
    Set<Floats> floats;
}
