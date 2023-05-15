package com.logistics.supply.service;

import com.logistics.supply.dto.LpoDraftDTO;
import com.logistics.supply.dto.RequestItemListDTO;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.exception.LpoNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.LocalPurchaseOrderDraftRepository;
import com.logistics.supply.util.EmailSenderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalPurchaseOrderDraftService {
  private final LocalPurchaseOrderDraftRepository localPurchaseOrderDraftRepository;
  private final QuotationService quotationService;
  private final RequestItemService requestItemService;
  private final EmailSenderUtil emailSenderUtil;
  private final EmployeeService employeeService;

  @Value("${config.lpo.template}")
  private String LPO_template;

  @Value("${config.mail.template}")
  String HOD_QUOTATION_REVIEW_TEMPLATE;

  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      cacheNames = {"lpoByRequestItemId", "lpoById", "lpoBySupplier", "lpoAwaitingApproval"},
      allEntries = true)
  public LocalPurchaseOrderDraft saveLPO(LocalPurchaseOrderDraft lpo) {
    return localPurchaseOrderDraftRepository.save(lpo);
  }

  @Transactional(rollbackFor = Exception.class)
  public LocalPurchaseOrderDraft createLPODraft(RequestItemListDTO requestItems) {
    Set<RequestItem> result =
        requestItemService.assignProcurementDetailsToItems(requestItems.getItems());
    LocalPurchaseOrderDraft lpo = new LocalPurchaseOrderDraft();
    lpo.setDeliveryDate(requestItems.getDeliveryDate());
    lpo.setRequestItems(result);
    lpo.setSupplierId(result.stream().findFirst().get().getSuppliedBy());
    Quotation quotation = quotationService.findById(requestItems.getQuotationId());
    lpo.setQuotation(quotation);
    LocalPurchaseOrderDraft localPurchaseOrderDraft = saveLPO(lpo);
    sendHodEmailOnQuotation(localPurchaseOrderDraft.getDepartment());
    return localPurchaseOrderDraft;
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
  public LocalPurchaseOrderDraft findLpoById(int lpoId) {
    return localPurchaseOrderDraftRepository
        .findById(lpoId)
        .orElseThrow(() -> new LpoNotFoundException(lpoId));
  }

  @Cacheable(value = "lpoBySupplier", key = "#supplierId")
  public List<LocalPurchaseOrderDraft> findLpoBySupplier(int supplierId) {
    return localPurchaseOrderDraftRepository.findBySupplierId(supplierId);
  }

  @Cacheable(value = "lpoAwaitingApproval")
  public List<LpoDraftDTO> findDraftAwaitingApproval() {
    List<LocalPurchaseOrderDraft> draftAwaitingApproval =
        localPurchaseOrderDraftRepository.findDraftAwaitingApproval();
    return draftAwaitingApproval.stream().map(LpoDraftDTO::toDto).collect(Collectors.toList());
  }

  @Cacheable(value = "lpoDraftAwaitingApproval")
  public List<LpoDraftDTO> findDraftDtoAwaitingApproval() {
    List<LocalPurchaseOrderDraft> draftAwaitingApproval =
        localPurchaseOrderDraftRepository.findDraftAwaitingApproval();
    return draftAwaitingApproval.stream().map(LpoDraftDTO::toDto).collect(Collectors.toList());
  }

  @Cacheable(value = "lpoDraftAwaitingApproval")
  public List<LpoDraftDTO> findDraftDtoAwaitingApprovalByHod(int departmentId) {
    List<LocalPurchaseOrderDraft> draftAwaitingApproval =
            localPurchaseOrderDraftRepository.findDraftAwaitingApprovalByHod(departmentId);
    return draftAwaitingApproval.stream().map(LpoDraftDTO::toDto).collect(Collectors.toList());
  }

  private void sendHodEmailOnQuotation(Department department) {
    CompletableFuture.runAsync(
        () -> {
          Employee employee = null;
          try {
            employee = employeeService.getDepartmentHOD(department);
          } catch (GeneralException e) {
            throw new RuntimeException(e);
          }
          String message =
              MessageFormat.format(
                  "Dear {0}, Kindly note that a quotation for an endorsed request is ready for review",
                  employee.getFullName());
          emailSenderUtil.sendComposeAndSendEmail(
              "REVIEW QUOTATION",
              message,
              HOD_QUOTATION_REVIEW_TEMPLATE,
              EmailType.HOD_REVIEW_QUOTATION,
              employee.getEmail());
        });
  }

  public void deleteLPO(int lpoId) {
    localPurchaseOrderDraftRepository.deleteById(lpoId);
  }
}
