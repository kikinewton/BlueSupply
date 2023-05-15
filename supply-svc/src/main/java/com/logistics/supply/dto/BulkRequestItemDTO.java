package com.logistics.supply.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Size;
import com.logistics.supply.model.RequestItem;
import java.util.List;

@Getter
@Setter
@ToString
public class BulkRequestItemDTO {

  @Size(min = 1)
  private List<RequestItem> requestItems;
}
