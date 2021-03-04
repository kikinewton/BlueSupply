package com.logistics.supply.dto;

import com.logistics.supply.model.Supplier;
import lombok.Getter;

@Getter
public class ProcurementDTO {

    private Float unitPrice;
    private Supplier supplier;
}
