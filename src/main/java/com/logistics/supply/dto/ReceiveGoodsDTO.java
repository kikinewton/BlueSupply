package com.logistics.supply.dto;

import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.RequestItem;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ReceiveGoodsDTO {
  InvoiceDTO invoice;
  List<RequestItem> requestItems;
  BigDecimal invoiceAmountPayable;
  LocalPurchaseOrder localPurchaseOrder;
  String comment;
}
