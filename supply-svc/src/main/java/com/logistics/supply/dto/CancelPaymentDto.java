package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CancelPaymentDto {

    private String chequeNumber;
    private String comment;
}
