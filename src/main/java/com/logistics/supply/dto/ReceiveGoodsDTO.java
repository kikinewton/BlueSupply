package com.logistics.supply.dto;

import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.RequestItem;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ReceiveGoodsDTO {
  @NotNull
  InvoiceDTO invoice;
  @Size(min = 1)
  List<RequestItem> requestItems;
  @Positive
  BigDecimal invoiceAmountPayable;
  @NotNull
  LocalPurchaseOrder localPurchaseOrder;
  String comment;
}
