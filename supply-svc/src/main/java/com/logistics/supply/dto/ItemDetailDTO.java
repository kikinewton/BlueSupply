package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ItemDetailDTO {
    private String itemName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;
    private String currency;
}
