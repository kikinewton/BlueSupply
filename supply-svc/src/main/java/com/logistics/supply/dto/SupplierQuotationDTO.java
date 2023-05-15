package com.logistics.supply.dto;

import com.logistics.supply.model.Quotation;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.logistics.supply.model.RequestItem;
import java.util.List;

/** Response for */
@Getter
@Setter
public class SupplierQuotationDTO {

  @Size(min = 1)
  private List<RequestItem> requestItems;

  @Valid private Quotation quotation;
}
