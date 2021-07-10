package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class RequestQuotationDTO {
    String name;
    Integer quantity;
    Date requestDate;
    Quotation quotation;
}
