package com.logistics.supply.dto;

import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@ToString
@Getter
public class SetSupplierDTO {
  @NotNull private Supplier supplier;

  @Size(min = 1)
  private Set<ItemAndPriceDTO> itemAndUnitPrice;

  @NotNull private RequestCategory requestCategory;
}
