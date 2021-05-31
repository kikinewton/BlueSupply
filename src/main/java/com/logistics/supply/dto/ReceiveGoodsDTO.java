package com.logistics.supply.dto;

import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.RequestItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReceiveGoodsDTO {
  InvoiceDTO invoice;
  List<RequestItem> requestItems;
  BigDecimal invoiceAmountPayable;
  LocalPurchaseOrder localPurchaseOrder;
  String comment;
}
