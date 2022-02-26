package com.logistics.supply.dto;

import com.logistics.supply.model.Invoice;
import com.logistics.supply.model.LocalPurchaseOrder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
public class GoodsReceivedNoteDTO {
    Invoice invoice;
    BigDecimal invoiceAmountPayable;
//    Integer supplier;
    LocalPurchaseOrder lpo;

}
