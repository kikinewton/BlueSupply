package com.logistics.supply.dto;


import com.logistics.supply.enums.RequestReason;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ReqItems {
    private String name;

    private RequestReason reason;

    private String purpose;

    private Integer quantity;
}

