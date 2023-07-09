package com.logistics.supply.dto;

import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.model.GoodsReceivedNote;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.PositiveOrZero;

import com.logistics.supply.enums.PaymentStatus;

import java.math.BigDecimal;

@Getter
@ToString
public class PaymentDraftDTO {

  private GoodsReceivedNote goodsReceivedNote;

  private BigDecimal paymentAmount;

  private PaymentStatus paymentStatus;

  private PaymentMethod paymentMethod;

  private String chequeNumber;

  private String bank;

  private String purchaseNumber;

  @PositiveOrZero
  private BigDecimal withholdingTaxPercentage;


}
