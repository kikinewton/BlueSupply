package com.logistics.supply.dto;

import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class QuotationDTO {
  private Supplier supplier;
  private RequestDocument requestDocument;
}
