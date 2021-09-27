package com.logistics.supply.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SupplierRequestDTO {
  List<SupplierRequest> supplierRequests;
}
