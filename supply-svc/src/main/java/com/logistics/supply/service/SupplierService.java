package com.logistics.supply.service;

import com.logistics.supply.dto.SupplierDto;
import com.logistics.supply.exception.SupplierNotFoundException;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SupplierService {

  private final SupplierRepository supplierRepository;

  @Cacheable(value = "supplierById", key = "{#supplierId}")
  public Supplier findById(int supplierId)  {
    return supplierRepository
        .findById(supplierId)
        .orElseThrow(() -> new SupplierNotFoundException(supplierId));
  }

  @Cacheable(value = "suppliers")
  public List<Supplier> findAll() {

    log.info("Fetch all suppliers");
    return supplierRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
  }

  public boolean existById(int supplierId) {
    return supplierRepository.existsById(supplierId);
  }

  @Cacheable(cacheNames = "#supplierById")
  public Supplier findBySupplierId(int supplierId) {
    return supplierRepository
        .findById(supplierId)
        .orElseThrow(() -> new SupplierNotFoundException(supplierId));
  }

  @Cacheable(cacheNames = "#supplierByName")
  public Supplier findByName(String name) {
    return supplierRepository
        .findByNameEqualsIgnoreCase(name)
        .orElseThrow(() -> new SupplierNotFoundException(name));
  }

  @Cacheable(cacheNames = "#suppliersByNameLikeList")
  public List<Integer> findByNameLike(String name) {
    return supplierRepository.findByNameContaining(name);
  }

  @Cacheable(cacheNames = "#supplierByNameLike")
  public Supplier findFirstByNameLike(String name) {

    log.info("Find supplier with name like: {}", name);
    return supplierRepository.findTopByNameContaining(name)
            .orElseThrow(() -> new SupplierNotFoundException(name));
  }

  public void delete(int supplierId) {
  }

  @CacheEvict(value = "suppliers", allEntries = true)
  public Supplier add(SupplierDto supplierDTO) {

    log.info("Add supplier with values {}", supplierDTO);
    Supplier supplier = new Supplier();
    BeanUtils.copyProperties(supplierDTO, supplier);
    return supplierRepository.save(supplier);
  }

  @CacheEvict(value = "supplierById", key = "#supplierId")
  @Transactional(rollbackFor = Exception.class)
  public Supplier updateSupplier(int supplierId, SupplierDto supplierDTO) {

    Supplier supplier1 =
        supplierRepository
            .findById(supplierId)
            .orElseThrow(() -> new SupplierNotFoundException(supplierId));
    if (Objects.nonNull(supplierDTO.getName())) supplier1.setName(supplierDTO.getName());
    if (Objects.nonNull(supplierDTO.getAccountNumber()))
      supplier1.setAccountNumber(supplierDTO.getAccountNumber());
    if (Objects.nonNull(supplierDTO.getPhoneNo()))
      supplier1.setPhoneNo(supplierDTO.getPhoneNo());
    if (Objects.nonNull(supplierDTO.getBank())) supplier1.setBank(supplierDTO.getBank());
    if (Objects.nonNull(supplierDTO.getLocation()))
      supplier1.setLocation(supplierDTO.getLocation());
    if (Objects.nonNull(supplierDTO.getEmail())) supplier1.setEmail(supplierDTO.getEmail());
    if (Objects.nonNull(supplierDTO.getDescription()))
      supplier1.setDescription(supplierDTO.getDescription());
    return supplierRepository.save(supplier1);
  }

  public List<Supplier> findSuppliersWithQuotationForLPO() {
    return supplierRepository.findSuppliersWithQuotationsWithoutLPO();
  }

  public List<Supplier> findSupplierWithNoDocAttachedToUnProcessedRequestItems() {
    return supplierRepository.findSupplierWithNoDocAttachedToUnProcessedRequestItems();
  }

  public List<Supplier> findUnRegisteredSupplierWithNoDocFromSRM() {
    return supplierRepository.findUnRegisteredSupplierWithNoDocAttachedFromSRM();
  }

  public List<Supplier> findUnRegisteredSuppliers() {
    return supplierRepository.findByRegisteredNotTrue();
  }
}
