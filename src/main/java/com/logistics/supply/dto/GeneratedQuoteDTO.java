package com.logistics.supply.dto;

import com.logistics.supply.model.Supplier;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class GeneratedQuoteDTO {
    Supplier supplierName;
    String phoneNo;
    String location;
    String deliveryDate;
    String productDescription;
}
