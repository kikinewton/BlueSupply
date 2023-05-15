package com.logistics.supply.dto;

import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeneratedQuoteDTO {
    private Supplier supplier;
    private String phoneNo;
    private String location;
    private String deliveryDate;
    private String productDescription;
    private List<ItemUpdateDTO> items;
}
