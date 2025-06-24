package com.logistics.supply.dto;

import lombok.Getter;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.model.Supplier;
import java.util.Set;

@ToString
@Getter
public class SetSupplierDTO {
  @NotNull private Supplier supplier;

  @Size(min = 1)
  private Set<ItemAndPriceDTO> itemAndUnitPrice;

  @NotNull private RequestCategory requestCategory;
}
