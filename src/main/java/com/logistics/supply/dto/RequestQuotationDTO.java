package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class RequestQuotationDTO {
    String name;
    Integer quantity;
    Date requestDate;
    Quotation quotation;
}
