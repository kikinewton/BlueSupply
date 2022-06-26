package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class QuotationAndRelatedRequestItemsDTO {
  Quotation quotation;
  List<RequestItemDTO> requestItems;
}
