package com.logistics.supply.dto;

import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDraftMinorDto extends MinorDto {
  private GrnMinorDto goodsReceivedNote;
  private BigDecimal paymentAmount;
  private PaymentStatus paymentStatus;
  private PaymentMethod paymentMethod;
  private String chequeNumber;
  private String bank;
  private String purchaseNumber;
  @PositiveOrZero private BigDecimal withholdingTaxPercentage;

  public static final PaymentDraftMinorDto toDto(Payment payment) {
    PaymentDraftMinorDto dto = new PaymentDraftMinorDto();
    dto.setId(payment.getId());
    dto.setPurchaseNumber(payment.getPurchaseNumber());
    dto.setPaymentAmount(payment.getPaymentAmount());
    dto.setPaymentStatus(payment.getPaymentStatus());
    dto.setPaymentMethod(payment.getPaymentMethod());
    dto.setChequeNumber(payment.getChequeNumber());
    dto.setBank(payment.getBank());
    dto.setWithholdingTaxPercentage(payment.getWithholdingTaxPercentage());
    dto.setGoodsReceivedNote(GrnMinorDto.toDto(payment.getGoodsReceivedNote()));
    return dto;
  }
}
