package com.logistics.supply.dto;

import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

@Getter
@ToString
public class InvoiceDTO {
    String invoiceNumber;
    int numberOfDaysToPayment;
    Supplier supplier;
    RequestDocument invoiceDocument;

}
