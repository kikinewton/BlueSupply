package com.logistics.supply.service;

import com.logistics.supply.dto.LpoDraftDTO;
import com.logistics.supply.dto.RequestItemListDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.LocalPurchaseOrderDraft;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.repository.LocalPurchaseOrderDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.LPO_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalPurchaseOrderDraftService {
  private final LocalPurchaseOrderDraftRepository localPurchaseOrderDraftRepository;
  private final QuotationService quotationService;
  private final RequestItemService requestItemService;

  @Value("${config.lpo.template}")
  private String LPO_template;

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      cacheNames = "#{#lpoByRequestItemId, #lpoById, #lpoBySupplier, #lpoAwaitingApproval}",
      allEntries = true)
  public LocalPurchaseOrderDraft saveLPO(LocalPurchaseOrderDraft lpo) {
    return localPurchaseOrderDraftRepository.save(lpo);
  }

  public LocalPurchaseOrderDraft createLPODraft(RequestItemListDTO requestItems) {
    Set<RequestItem> result =
        requestItemService.assignProcurementDetailsToItems(requestItems.getItems());
    LocalPurchaseOrderDraft lpo = new LocalPurchaseOrderDraft();
    lpo.setDeliveryDate(requestItems.getDeliveryDate());
    lpo.setRequestItems(result);
    lpo.setSupplierId(result.stream().findFirst().get().getSuppliedBy());
    Quotation quotation = quotationService.findById(requestItems.getQuotationId());
    lpo.setQuotation(quotation);
    return saveLPO(lpo);
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


  @Cacheable(value = "lpoDraftAwaitingApproval")
  public List<LpoDraftDTO> findDraftDtoAwaitingApproval() {
    List<LocalPurchaseOrderDraft> draftAwaitingApproval =
        localPurchaseOrderDraftRepository.findDraftAwaitingApproval();
    return draftAwaitingApproval.stream()
        .map(LpoDraftDTO::toDto)
        .collect(Collectors.toList());
  }

  public void deleteLPO(int lpoId) {
    localPurchaseOrderDraftRepository.deleteById(lpoId);
  }
}
