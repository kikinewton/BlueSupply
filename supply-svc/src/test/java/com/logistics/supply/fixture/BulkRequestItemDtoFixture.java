package com.logistics.supply.fixture;

import com.logistics.supply.dto.BulkRequestItemDto;
import com.logistics.supply.model.RequestItem;

import java.util.List;

public class BulkRequestItemDtoFixture {

  BulkRequestItemDtoFixture() {}

  public static BulkRequestItemDto getBulkRequestItemDto() {
    RequestItem requestItem = new RequestItem();
    requestItem.setId(100);
    List<RequestItem> requestItems = List.of(requestItem);
    BulkRequestItemDto bulkRequestItemDto = new BulkRequestItemDto();
    bulkRequestItemDto.setRequestItems(requestItems);
    return bulkRequestItemDto;
  }
}
