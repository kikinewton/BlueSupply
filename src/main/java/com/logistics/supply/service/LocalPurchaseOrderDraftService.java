package com.logistics.supply.service;

import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.LocalPurchaseOrderDraft;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.repository.LocalPurchaseOrderDraftRepository;
import com.logistics.supply.repository.RoleRepository;
import com.logistics.supply.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.logistics.supply.util.Constants.LPO_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalPurchaseOrderDraftService {

  private static final String PDF_RESOURCES = "/pdf-resources/";
  final LocalPurchaseOrderDraftRepository localPurchaseOrderDraftRepository;
  final RoleRepository roleRepository;
  final SupplierRepository supplierRepository;
  final EmployeeRepository employeeRepository;
  final RequestDocumentService requestDocumentService;

  @Value("${config.lpo.template}")
  private String LPO_template;

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      cacheNames = "#{#lpoByRequestItemId, #lpoById, #lpoBySupplier, #lpoAwaitingApproval }",
      allEntries = true)
  public LocalPurchaseOrderDraft saveLPO(LocalPurchaseOrderDraft lpo) {
    return localPurchaseOrderDraftRepository.save(lpo);
  }

  public long count() {
    return localPurchaseOrderDraftRepository.count() + 1;
  }

  @Cacheable(value = "lpoByRequestItemId", key = "#requestItemId")
  public LocalPurchaseOrderDraft findByRequestItemId(int requestItemId) {
    return localPurchaseOrderDraftRepository.findLpoByRequestItem(requestItemId);
  }

  public List<LocalPurchaseOrderDraft> findAll() {
    return localPurchaseOrderDraftRepository.findAll();
  }

  @Cacheable(value = "lpoById", key = "#lpoId")
  public LocalPurchaseOrderDraft findLpoById(int lpoId) throws GeneralException {
    return localPurchaseOrderDraftRepository
        .findById(lpoId)
        .orElseThrow(() -> new GeneralException(LPO_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @Cacheable(value = "lpoBySupplier", key = "#supplierId")
  public List<LocalPurchaseOrderDraft> findLpoBySupplier(int supplierId) {
    return localPurchaseOrderDraftRepository.findBySupplierId(supplierId);
  }

  @Cacheable(value = "lpoAwaitingApproval")
  public List<LocalPurchaseOrderDraft> findDraftAwaitingApproval() {
    return localPurchaseOrderDraftRepository.findDraftAwaitingApproval();
  }

  public void deleteLPO(int lpoId) {
    localPurchaseOrderDraftRepository.deleteById(lpoId);
  }
}
