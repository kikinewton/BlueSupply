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
public class PaymentDraftMinorDto extends MinorDto {
  private GrnMinorDto goodsReceivedNote;
  private BigDecimal paymentAmount;
  private PaymentStatus paymentStatus;
  private PaymentMethod paymentMethod;
  private String chequeNumber;
  private String bank;
  private String purchaseNumber;
  @PositiveOrZero private BigDecimal withholdingTaxPercentage;

  public static final PaymentDraftMinorDto toDto(PaymentDraft paymentDraft) {
    PaymentDraftMinorDto paymentDraftMinorDTO = new PaymentDraftMinorDto();
    BeanUtils.copyProperties(paymentDraft, paymentDraftMinorDTO);
    GrnMinorDto grnMinorDTO = GrnMinorDto.toDto(paymentDraft.getGoodsReceivedNote());
    paymentDraftMinorDTO.setGoodsReceivedNote(grnMinorDTO);
    return paymentDraftMinorDTO;
  }
}
