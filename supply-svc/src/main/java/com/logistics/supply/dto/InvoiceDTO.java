package com.logistics.supply.dto;

import lombok.Getter;
import lombok.ToString;

import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.Supplier;
import java.util.Date;

@Getter
@ToString
public class InvoiceDTO {
    private String invoiceNumber;
    private Date paymentDate;
    private Supplier supplier;
    private RequestDocument invoiceDocument;

}
