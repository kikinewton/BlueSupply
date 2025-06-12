package com.logistics.supply.dto;

import com.logistics.supply.enums.PaymentMethod;
import lombok.*;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.logistics.supply.enums.PaymentStatus;
import java.math.BigDecimal;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
  private String purchaseNumber;
  private BigDecimal paymentAmount;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  private String chequeNumber;
  private String bank;
}
