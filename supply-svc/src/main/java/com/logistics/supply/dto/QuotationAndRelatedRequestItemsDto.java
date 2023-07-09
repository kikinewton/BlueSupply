package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.logistics.supply.model.Quotation;
import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class QuotationAndRelatedRequestItemsDto {

  private Quotation quotation;
  private List<RequestItemDto> requestItems;
}
