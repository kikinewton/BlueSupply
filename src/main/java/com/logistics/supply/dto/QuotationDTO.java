package com.logistics.supply.dto;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@ToString
public class QuotationDTO {
  private Supplier supplier;
  private RequestDocument requestDocument;
}
