package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Getter
@ToString
public class BulkRequestItemDTO {
  private Set<RequestItem> requestItems;
}
