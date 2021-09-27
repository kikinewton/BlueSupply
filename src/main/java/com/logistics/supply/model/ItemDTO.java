package com.logistics.supply.model;

import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class ItemDTO {

    @Column(nullable = false, updatable = false)
    @ValidName
    private String name;

    @Column(nullable = false, updatable = false)
    @ValidDescription
    private String purpose;

    @Column(nullable = false)
    @Positive
    private Integer quantity;

    @Column @PositiveOrZero
    private BigDecimal unitPrice = BigDecimal.valueOf(0);

    @Column @PositiveOrZero private BigDecimal totalPrice = BigDecimal.valueOf(0);


}
