package com.logistics.supply.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemDetailDTO {

    private String itemName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;

    public ItemDetailDTO() {
    }
}
