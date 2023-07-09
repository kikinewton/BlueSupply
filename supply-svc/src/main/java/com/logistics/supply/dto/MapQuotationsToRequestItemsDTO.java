package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import lombok.Getter;
import lombok.ToString;

import com.logistics.supply.model.RequestItem;
import java.util.List;
import java.util.Set;

@Getter
@ToString
public class MapQuotationsToRequestItemsDTO {

  private Set<RequestItem> requestItems;
  private List<Quotation> quotations;
}
