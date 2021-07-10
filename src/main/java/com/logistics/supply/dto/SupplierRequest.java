package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class SupplierRequest {
    int supplierId;
    String supplierName;
    Set<RequestItem> requests;
}
