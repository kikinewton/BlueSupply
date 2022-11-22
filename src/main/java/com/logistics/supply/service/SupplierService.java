package com.logistics.supply.service;

import com.logistics.supply.dto.SupplierDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.logistics.supply.util.Constants.SUPPLIER_NOT_FOUND;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SupplierService {
  private final SupplierRepository supplierRepository;

  @SneakyThrows
  @Cacheable(value = "supplierById", key = "{ #supplierId }")
  public Supplier findById(int supplierId)  {
    return supplierRepository
        .findById(supplierId)
        .orElseThrow(() -> new GeneralException(SUPPLIER_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @Cacheable(value = "suppliers")
  public List<Supplier> getAll() {
    return supplierRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
  }

  public boolean existById(int supplierId) {
    return supplierRepository.existsById(supplierId);
  }

  public Supplier findBySupplierId(int supplierId) throws GeneralException {
    return supplierRepository
        .findById(supplierId)
        .orElseThrow(() -> new GeneralException(SUPPLIER_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public Supplier findByName(String name) throws GeneralException {
    return supplierRepository
        .findByName(name)
        .orElseThrow(() -> new GeneralException(SUPPLIER_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  public void delete(int supplierId) {
    return;
  }

  @CacheEvict(value = "suppliers", allEntries = true)
  public Supplier add(Supplier supplier) {
    return supplierRepository.save(supplier);
  }

  @CacheEvict(value = "supplierById", key = "#supplierId")
  @Transactional(rollbackFor = Exception.class)
  public Supplier updateSupplier(int supplierId, SupplierDTO supplierDTO) throws GeneralException {
    Supplier supplier1 =
        supplierRepository
            .findById(supplierId)
            .orElseThrow(() -> new GeneralException(SUPPLIER_NOT_FOUND, HttpStatus.NOT_FOUND));
    if (Objects.nonNull(supplierDTO.getName())) supplier1.setName(supplierDTO.getName());
    if (Objects.nonNull(supplierDTO.getAccountNumber()))
      supplier1.setAccountNumber(supplierDTO.getAccountNumber());
    if (Objects.nonNull(supplierDTO.getPhone_no()))
      supplier1.setPhone_no(supplierDTO.getPhone_no());
    if (Objects.nonNull(supplierDTO.getBank())) supplier1.setBank(supplierDTO.getBank());
    if (Objects.nonNull(supplierDTO.getLocation()))
      supplier1.setLocation(supplierDTO.getLocation());
    if (Objects.nonNull(supplierDTO.getEmail())) supplier1.setEmail(supplierDTO.getEmail());
    if (Objects.nonNull(supplierDTO.getDescription()))
      supplier1.setDescription(supplierDTO.getDescription());
    return supplierRepository.save(supplier1);
  }

  public List<Supplier> findSuppliersWithNonFinalProcurement() {
    return supplierRepository.findSuppliersWithNonFinalRequestProcurement();
  }

  public List<Supplier> findSuppliersWithQuotationForLPO() {
    return supplierRepository.findSuppliersWithQuotationsWithoutLPO();
  }

  @Cacheable(value = "suppliersWithoutDocumentInQuotation")
  public List<Supplier> findSuppliersWithoutDocumentInQuotation() {
    return supplierRepository.findSuppliersWithoutDocumentInQuotation();
  }

  public List<Supplier> findSupplierWithNoDocFromSRM() {
    return supplierRepository.findSupplierWithNoDocAttachedFromSRM();
  }

  public List<Supplier> findUnRegisteredSupplierWithNoDocFromSRM() {
    return supplierRepository.findUnRegisteredSupplierWithNoDocAttachedFromSRM();
  }

  public List<Supplier> findUnRegisteredSuppliers() {
    return supplierRepository.findByRegisteredNotTrue();
  }
}
