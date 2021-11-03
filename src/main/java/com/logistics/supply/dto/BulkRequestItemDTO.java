package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ToString
public class BulkRequestItemDTO {

  @Size(min = 1)
  private List<RequestItem> requestItems;
}
