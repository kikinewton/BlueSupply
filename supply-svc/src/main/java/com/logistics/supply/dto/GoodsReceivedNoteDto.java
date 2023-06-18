package com.logistics.supply.dto;

import lombok.Getter;
import lombok.ToString;

import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.LocalPurchaseOrder;
import java.math.BigDecimal;

@Getter
@ToString
public class GoodsReceivedNoteDTO extends MinorDTO {
  private Invoice invoice;
  private BigDecimal invoiceAmountPayable;
  private LocalPurchaseOrder lpo;
}
