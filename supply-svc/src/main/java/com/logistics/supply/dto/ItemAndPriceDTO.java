package com.logistics.supply.dto;

import lombok.Getter;

import com.logistics.supply.model.RequestItem;
import java.math.BigDecimal;

@Getter
public class ItemAndPriceDTO {
    private RequestItem requestItem;
    private BigDecimal unitPrice;

}
