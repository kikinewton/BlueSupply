package com.logistics.supply.dto;

import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReason;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Supplier;
import lombok.Getter;


@Getter
public class RequestItemDTO {
    private String name;

    private RequestReason reason;

    private String purpose;

    private Integer quantity;

    private Float unitPrice;

    private Supplier supplier;

    private RequestStatus status;

    private RequestApproval approval;

    private Employee employee;

}
