package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@ToString
public class MapQuotationsToRequestItemsDTO {

  private Set<RequestItem> requestItems;
  private Set<Quotation> quotations;
}
