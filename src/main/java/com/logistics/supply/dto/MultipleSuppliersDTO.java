package com.logistics.supply.dto;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class MultipleSuppliersDTO {
  private Employee procurementOfficer;
  private RequestCategory requestCategory;
  private Set<Supplier> suppliers;
}