package com.logistics.supply.dto;


import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.enums.RequestType;
import com.logistics.supply.model.Department;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ReqItems {
    private String name;

    private RequestReason reason;

    private String purpose;

    private Integer quantity;

    private Department userDepartment;

    private RequestType requestType;
}

