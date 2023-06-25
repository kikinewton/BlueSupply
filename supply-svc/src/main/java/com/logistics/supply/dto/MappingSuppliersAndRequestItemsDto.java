package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import lombok.*;

import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MappingSuppliersAndRequestItemsDto {

  @Size(min = 1, message = "Suppliers must not be empty")
  private Set<Supplier> suppliers;

  @Size(min = 1, message = "Request items must not be empty")
  private Set<RequestItem> requestItems;
}
