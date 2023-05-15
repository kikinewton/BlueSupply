package com.logistics.supply.exception;

public class SupplierNotFoundException extends NotFoundException {

    public SupplierNotFoundException(String supplierName) {
        super("Supplier with name: %s not found".formatted(supplierName));
    }

    public SupplierNotFoundException(int supplierId) {
        super("Supplier with id: %s not found".formatted(supplierId));
    }
}
