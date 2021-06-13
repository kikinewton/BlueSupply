package com.logistics.supply.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SupplierRequestDTO {
  List<SupplierRequest> supplierRequests;
}
