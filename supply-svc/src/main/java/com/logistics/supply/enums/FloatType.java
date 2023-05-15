package com.logistics.supply.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FloatType {
    SERVICE("SERVICE"),
    GOODS("GOODS");

    private String floatType;
}