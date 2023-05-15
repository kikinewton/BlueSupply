package com.logistics.supply.dto;

import lombok.Getter;
import lombok.Setter;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import java.util.Set;

@Getter
@Setter
public class MappingSuppliersAndRequestItemsDTO {

  private Employee procurementOfficer;
  private Set<Supplier> suppliers;
  private Set<RequestItem> requestItems;
}
