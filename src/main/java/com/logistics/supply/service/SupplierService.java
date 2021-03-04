package com.logistics.supply.service;

import com.logistics.supply.model.Supplier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierService extends AbstractDataService {

  public List<Supplier> getAll() {
    List<Supplier> suppliers = new ArrayList<>();
    try {
      List<Supplier> supplierList = supplierRepository.findAll();
      suppliers.addAll(supplierList);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return suppliers;
  }

  public Optional<Supplier> findBySupplierId(int supplierId) {
    Optional<Supplier> supplier = Optional.empty();
    try {
      supplier = supplierRepository.findById(supplierId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return supplier;
  }

  public Optional<Supplier> findByName(String name) {
    Optional<Supplier> supplier = Optional.empty();
    try {
      supplier = supplierRepository.findByName(name);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return supplier;
  }

  public void delete(int supplierId) {
    try {
      supplierRepository.deleteById(supplierId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Supplier add(Supplier supplier) {
    try {
      return supplierRepository.save(supplier);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
