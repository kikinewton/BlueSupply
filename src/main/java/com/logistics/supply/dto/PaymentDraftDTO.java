package com.logistics.supply.dto;

import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.GoodsReceivedNote;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
public class PaymentDraftDTO {

  GoodsReceivedNote goodsReceivedNote;

  BigDecimal paymentAmount;

  PaymentStatus paymentStatus;

  PaymentMethod paymentMethod;

  String chequeNumber;

  String bank;

  String purchaseNumber;

}
