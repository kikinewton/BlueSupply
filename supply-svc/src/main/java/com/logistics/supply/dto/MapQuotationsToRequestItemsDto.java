package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.logistics.supply.model.RequestItem;
import java.util.List;
import java.util.Set;

@Getter
@ToString
@Setter
@AllArgsConstructor
public class MapQuotationsToRequestItemsDto {

  private Set<RequestItem> requestItems;
  private List<Quotation> quotations;
}
