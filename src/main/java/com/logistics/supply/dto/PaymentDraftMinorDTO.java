package com.logistics.supply.dto;

import com.logistics.supply.enums.PaymentMethod;
import com.logistics.supply.enums.PaymentStatus;
import com.logistics.supply.model.PaymentDraft;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDraftMinorDTO extends MinorDTO {
  GrnMinorDTO goodsReceivedNote;
  BigDecimal paymentAmount;
  PaymentStatus paymentStatus;
  PaymentMethod paymentMethod;
  String chequeNumber;
  String bank;
  String purchaseNumber;
  @PositiveOrZero private BigDecimal withholdingTaxPercentage;

  public static final PaymentDraftMinorDTO toDto(PaymentDraft paymentDraft) {
    PaymentDraftMinorDTO paymentDraftMinorDTO = new PaymentDraftMinorDTO();
    BeanUtils.copyProperties(paymentDraft, paymentDraftMinorDTO);
    GrnMinorDTO grnMinorDTO = GrnMinorDTO.toDto(paymentDraft.getGoodsReceivedNote());
    paymentDraftMinorDTO.setGoodsReceivedNote(grnMinorDTO);
    return paymentDraftMinorDTO;
  }
}
