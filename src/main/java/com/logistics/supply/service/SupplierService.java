package com.logistics.supply.service;

import com.logistics.supply.dto.SupplierDTO;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

  private final SupplierRepository supplierRepository;
  private final RequestItemRepository requestItemRepository;

  @Cacheable(value = "supplierById", key = "{ #supplierId }")
  public Supplier findById(int supplierId) {
    try {
      Optional<Supplier> s = supplierRepository.findById(supplierId);
      if (s.isPresent()) return s.get();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  @Cacheable(value = "suppliers")
  public List<Supplier> getAll() {
    List<Supplier> suppliers = new ArrayList<>();
    try {
      List<Supplier> supplierList = supplierRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
      suppliers.addAll(supplierList);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return suppliers;
  }

  public boolean existById(int supplierId) {
    return supplierRepository.existsById(supplierId);
  }

  public Optional<Supplier> findBySupplierId(int supplierId) {
    Optional<Supplier> supplier = Optional.empty();
    try {
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

  @CacheEvict(value = "suppliers", allEntries = true)
  public Supplier add(Supplier supplier) {
    try {
      return supplierRepository.save(supplier);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  @CacheEvict(value = "supplierById", key = "#supplierId")
  @Transactional(rollbackFor = Exception.class)
  public Supplier updateSupplier(int supplierId, SupplierDTO supplierDTO) {
    log.info(supplierDTO.toString());
    try {
      Optional<Supplier> supplier =
          supplierRepository
              .findById(supplierId)
              .map(
                  x -> {
                    if (Objects.nonNull(supplierDTO.getName())) x.setName(supplierDTO.getName());
                    if (Objects.nonNull(supplierDTO.getAccountNumber()))
                      x.setAccountNumber(supplierDTO.getAccountNumber());
                    if (Objects.nonNull(supplierDTO.getPhone_no()))
                      x.setPhone_no(supplierDTO.getPhone_no());
                    if (Objects.nonNull(supplierDTO.getBank())) x.setBank(supplierDTO.getBank());
                    if (Objects.nonNull(supplierDTO.getLocation()))
                      x.setLocation(supplierDTO.getLocation());
                    if (Objects.nonNull(supplierDTO.getEmail())) x.setEmail(supplierDTO.getEmail());
                    if (Objects.nonNull(supplierDTO.getDescription()))
                      x.setDescription(supplierDTO.getDescription());
                    try {
                      return supplierRepository.save(x);
                    } catch (Exception e) {
                      log.error(e.toString());
                    }
                    return null;
                  });

      if (supplier.isPresent()) return supplier.get();
      log.info("Supplier id not updated");

    } catch (Exception e) {
      log.error(e.toString());
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
      suppliers.addAll(supplierRepository.findSuppliersWithQuotationsWithoutLPO());
      // items without lpo
      Set<Integer> supplierIds =
          requestItemRepository.findRequestItemsWithLpo().stream()
              .map(RequestItem::getSuppliedBy)
              .collect(Collectors.toSet());

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

  public List<Supplier> findUnRegisteredSupplierWithNoDocFromSRM() {
    List<Supplier> suppliers = new ArrayList<>();
    try {
      suppliers.addAll(supplierRepository.findUnRegisteredSupplierWithNoDocAttachedFromSRM());
      return suppliers;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return suppliers;
  }

  public List<Supplier> findUnRegisteredSuppliers() {
    return supplierRepository.findByRegisteredNotTrue();
  }
}
