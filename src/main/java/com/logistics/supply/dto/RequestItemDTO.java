package com.logistics.supply.dto;

import com.logistics.supply.enums.PriorityLevel;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.model.Employee;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class RequestItemDTO {
    private String name;

    private RequestReason reason;

    private String purpose;

    private int quantity;

    private PriorityLevel priorityLevel;

    private Employee employee;

}
