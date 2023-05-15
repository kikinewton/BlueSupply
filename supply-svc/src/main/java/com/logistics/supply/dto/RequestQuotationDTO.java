package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class RequestQuotationDTO {
    private String name;
    private Integer quantity;
    private Date requestDate;
    private Quotation quotation;
}
