package com.logistics.supply.dto;

import lombok.*;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ItemUpdateDto {

    @Positive
    private Integer quantity;

    private String description;

    private BigDecimal estimatedPrice;
}
