package com.logistics.supply.dto;

import lombok.Data;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class ItemUpdateDTO {

    @Positive
    private Integer quantity;

    private String description;

    private BigDecimal estimatedPrice;
}
