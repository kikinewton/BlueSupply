package com.logistics.supply.dto;

import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.ItemDTO;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class FloatDTO {

    ItemDTO item;
    BigDecimal paymentAmount;
}
