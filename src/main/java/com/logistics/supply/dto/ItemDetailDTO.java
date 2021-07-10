package com.logistics.supply.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemDetailDTO {

    private String itemName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;

    public ItemDetailDTO() {
    }
}
