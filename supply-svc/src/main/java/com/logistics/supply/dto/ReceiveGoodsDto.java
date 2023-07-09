package com.logistics.supply.dto;

import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
@ToString
@Getter
@Setter
public class ReceiveGoodsDto {
  @NotNull
  private InvoiceDto invoice;
  @Size(min = 1)
  private List<RequestItem> requestItems;
  @Positive
  private BigDecimal invoiceAmountPayable;
  @NotNull
  private LocalPurchaseOrder localPurchaseOrder;
  private String comment;
}
