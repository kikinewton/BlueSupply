package com.logistics.supply.dto;

import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeneratedQuoteDTO {
    Supplier supplier;
    String phoneNo;
    String location;
    String deliveryDate;
    String productDescription;
    List<ItemUpdateDTO> items;
}
