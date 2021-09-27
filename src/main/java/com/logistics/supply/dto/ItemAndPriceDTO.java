package com.logistics.supply.dto;

import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.model.RequestItem;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ItemAndPriceDTO {
    private RequestItem requestItem;
    private BigDecimal unitPrice;

}
