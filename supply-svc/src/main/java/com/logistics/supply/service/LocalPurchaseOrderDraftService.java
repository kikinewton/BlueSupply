package com.logistics.supply.service;

import com.logistics.supply.dto.LpoDTO;
import com.logistics.supply.dto.LpoDraftDto;
import com.logistics.supply.dto.RequestItemListDTO;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.exception.LpoNotFoundException;
import com.logistics.supply.exception.SupplierNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.LocalPurchaseOrderDraftRepository;
import com.logistics.supply.repository.LocalPurchaseOrderRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.logistics.supply.util.EmailSenderUtil;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalPurchaseOrderDraftService {
  private final SupplierRepository supplierRepository;

  private final LocalPurchaseOrderRepository localPurchaseOrderRepository;
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

  public Page<LocalPurchaseOrderDraft> findAll(Pageable pageable) {
    return localPurchaseOrderDraftRepository.findAll(pageable);
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
  public List<LpoDraftDto> findDraftAwaitingApproval() {
    List<LocalPurchaseOrderDraft> draftAwaitingApproval =
        localPurchaseOrderDraftRepository.findDraftAwaitingApproval();
    return draftAwaitingApproval.stream().map(LpoDraftDto::toDto).collect(Collectors.toList());
  }

  @Cacheable(value = "lpoDraftAwaitingApproval", key = "{#pageable.getPageSize(), #pageable.getPageNumber()}")
  public Page<LpoDraftDto> findDraftDtoAwaitingApproval(Pageable pageable) {

    Page<LocalPurchaseOrderDraft> draftAwaitingApproval =
        localPurchaseOrderDraftRepository.findDraftAwaitingApproval(pageable);
    return draftAwaitingApproval.map(LpoDraftDto::toDto);
  }

  @Cacheable(value = "lpoDraftAwaitingApproval",
          key = "{#departmentId, #pageable.getPageSize(), #pageable.getPageNumber()}")
  public Page<LpoDraftDto> findDraftDtoAwaitingApprovalByHod(int departmentId, Pageable pageable) {

    log.info("Fetch lpo draft awaiting review for department id: {}", departmentId);
    Page<LocalPurchaseOrderDraft> draftAwaitingApproval =
        localPurchaseOrderDraftRepository.findDraftAwaitingApprovalByHod(departmentId, pageable);
    return draftAwaitingApproval.map(LpoDraftDto::toDto);
  }

  private void sendHodEmailOnQuotation(Department department) {
    CompletableFuture.runAsync(
        () -> {
          Employee employee = employeeService.getDepartmentHOD(department);

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

  public LocalPurchaseOrder createLpoFromDraft(LpoDTO lpoDto) {
    LocalPurchaseOrderDraft draft = findLpoById(lpoDto.getDraftId());
    LocalPurchaseOrder lpo = new LocalPurchaseOrder();
    Employee generalManager = employeeService.getGeneralManager();
    lpo.setApprovedBy(generalManager);
    Set<RequestItem> items =
            draft.getRequestItems().stream()
                    .filter(i -> i.getApproval() == RequestApproval.APPROVED)
                    .collect(Collectors.toSet());
    lpo.setRequestItems(items);
    lpo.setSupplierId(draft.getSupplierId());
    lpo.setQuotation(draft.getQuotation());
    String count = String.valueOf(count());
    String department =
            lpo.getRequestItems().stream().findAny().get().getUserDepartment().getName();
    String ref = IdentifierUtil.idHandler("LPO", department, count);
    lpo.setLpoRef(ref);
    lpo.setIsApproved(true);
    lpo.setDeliveryDate(draft.getDeliveryDate());
    lpo.setLocalPurchaseOrderDraft(draft);
    lpo.setDepartment(draft.getDepartment());
    return localPurchaseOrderRepository.save(lpo);
  }

  public Page<LpoDraftDto> findBySupplierName(String supplierName, Pageable pageable) {

    log.info("Fetch Lpo draft by supplier name: {}", supplierName);
    Optional<Supplier> supplier = supplierRepository.findByNameEqualsIgnoreCase(supplierName);
    if(!supplier.isPresent()) throw new SupplierNotFoundException(supplierName);
    return localPurchaseOrderDraftRepository.findBySupplierId(supplier.get().getId(), pageable)
            .map(LpoDraftDto::toDto);
  }
}
