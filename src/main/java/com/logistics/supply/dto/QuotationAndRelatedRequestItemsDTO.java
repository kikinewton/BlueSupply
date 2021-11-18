package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestItem;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Data
@ToString
@NoArgsConstructor
public class QuotationAndRelatedRequestItemsDTO {
  Quotation quotation;
  List<RequestItem> requestItems;
}
