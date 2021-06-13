package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SupplierRequest {
    int supplierId;
    String supplierName;
    List<RequestItem> requests;
}
