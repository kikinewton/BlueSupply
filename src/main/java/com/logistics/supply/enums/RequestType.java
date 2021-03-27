package com.logistics.supply.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum RequestType {
    SERVICE_REQUEST("SERVICE-REQUEST"),GOODS_REQUEST("GOODS-REQUEST"), PROJECT_AND_WORKS("PROJECT-AND-WORKS");

    private String requestType;

}
