package com.logistics.supply.dto;

import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;


@ToString
@Getter
public class SetSupplierDTO {
    private Supplier supplier;
    private Set<ItemAndPriceDTO> itemAndUnitPrice;
    private RequestCategory requestCategory;


}
