package com.logistics.supply.service;

import com.logistics.supply.dto.SupplierDTO;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.SupplierRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SupplierService {

  @Autowired SupplierRepository supplierRepository;

  public List<Supplier> getAll() {
    List<Supplier> suppliers = new ArrayList<>();
    try {
      List<Supplier> supplierList = supplierRepository.findAll();
      suppliers.addAll(supplierList);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return suppliers;
  }

  public Optional<Supplier> findBySupplierId(int supplierId) {
    Optional<Supplier> supplier = Optional.empty();
    try {
      System.out.println("find suppliers");
      supplier = supplierRepository.findById(supplierId);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return supplier;
  }

  public Optional<Supplier> findByName(String name) {
    Optional<Supplier> supplier = Optional.empty();
    try {
      supplier = supplierRepository.findByName(name);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return supplier;
  }

  public void delete(int supplierId) {
    try {
      supplierRepository.deleteById(supplierId);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  public Supplier add(Supplier supplier) {
    try {
      return supplierRepository.save(supplier);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public Supplier edit(int supplierId, SupplierDTO supplierDTO) {
    Optional<Supplier> supplier = supplierRepository.findById(supplierId);
    if (supplier.isPresent()) {
      BeanUtils.copyProperties(supplierDTO, supplier.get());
      try {
        return supplierRepository.save(supplier.get());
      } catch (Exception e) {
        log.error(e.toString());
      }
    }
    return null;
  }

  public List<Supplier> findSuppliersWithNonFinalProcurement() {
    List<Supplier> suppliers = new ArrayList<>();
    try {
      suppliers.addAll(supplierRepository.findSuppliersWithNonFinalRequestProcurement());
      return suppliers;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return suppliers;
  }

  public List<Supplier> findSuppliersWithQuotationForLPO() {
    List<Supplier> suppliers = new ArrayList<>();
    try {
      suppliers.addAll(supplierRepository.findSuppliersWithQuotation());
      return suppliers;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return suppliers;
  }

  public List<Supplier> findSuppliersWithoutDocumentInQuotation() {
    List<Supplier> suppliers = new ArrayList<>();
    try {
      suppliers.addAll(supplierRepository.findSuppliersWithoutDocumentInQuotation());
      return suppliers;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return suppliers;
  }

  public List<Supplier> findSupplierWithNoDocFromSRM() {
    List<Supplier> suppliers = new ArrayList<>();
    try {
      suppliers.addAll(supplierRepository.findSupplierWithNoDocAttachedFromSRM());
      return suppliers;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return suppliers;
  }
}
