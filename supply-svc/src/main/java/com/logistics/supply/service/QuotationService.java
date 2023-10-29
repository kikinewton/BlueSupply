package com.logistics.supply.service;

import com.logistics.supply.dto.*;
import com.logistics.supply.enums.EmailType;
import com.logistics.supply.event.AssignQuotationRequestItemEvent;
import com.logistics.supply.exception.NoQuotationsToUpdateException;
import com.logistics.supply.exception.QuotationNotFoundException;
import com.logistics.supply.exception.RequestDocumentNotFoundException;
import com.logistics.supply.exception.SupplierNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.QuotationRepository;
import com.logistics.supply.repository.RequestDocumentRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.logistics.supply.repository.SupplierRequestMapRepository;
import com.logistics.supply.specification.GenericSpecification;
import com.logistics.supply.specification.SearchCriteria;
import com.logistics.supply.specification.SearchOperation;
import com.logistics.supply.util.EmailSenderUtil;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuotationService {

  private final EmailSenderUtil senderUtil;

  private final QuotationRepository quotationRepository;
  private final SupplierRepository supplierRepository;
  private final RequestItemService requestItemService;
  private final SupplierRequestMapRepository supplierRequestMapRepository;
  private final RequestDocumentRepository requestDocumentRepository;
  private final EmployeeService employeeService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Value("${config.mail.template}")
  String emailTemplate;

  @Transactional
  @CacheEvict(value = {
          "allQuotations", "allQuotationsPage", "quotationById", "quotationBySupplier",
          "quotationNotLinkedToLpoWithRequestItems", "quotationUnderHodReviewWithRequestItems",
          "quotationsLinkedToLPOByDepartment", "quotationsLinkedToLpo", "approvedQuotationsBySupplier",
          "quotationsNonExpiredNotLinkedToLPO", "supplierQuotationDto"
  }, allEntries = true)
  public Quotation createQuotation(CreateQuotationRequest quotationRequest) {
    RequestDocument requestDocument =
        requestDocumentRepository
            .findById(quotationRequest.getDocumentId())
            .orElseThrow(
                () -> new RequestDocumentNotFoundException(quotationRequest.getDocumentId()));

    Supplier supplier =
        supplierRepository
            .findById(quotationRequest.getSupplierId())
            .orElseThrow(() -> new SupplierNotFoundException(quotationRequest.getSupplierId()));

    Quotation quotation = new Quotation();
    quotation.setSupplier(supplier);
    long count = quotationRepository.count();

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    Employee employee = employeeService.findEmployeeByEmail(username);
    quotation.setCreatedBy(employee);

    String ref = IdentifierUtil.idHandler("QUO", supplier.getName(), String.valueOf(count));
    quotation.setQuotationRef(ref);
    quotation.setRequestDocument(requestDocument);
    Quotation savedQuotation = save(quotation);
    sendCreateQuotationEvent(quotationRequest, supplier, savedQuotation);
    return savedQuotation;
  }

  private void sendCreateQuotationEvent(CreateQuotationRequest quotationRequest,
                                        Supplier supplier,
                                        Quotation savedQuotation) {

    CompletableFuture.runAsync(
        () -> {
          Set<RequestItem> requestItems =
              quotationRequest.getRequestItemIds().stream()
                  .map(requestItemService::findById)
                  .collect(Collectors.toSet());

          requestItems
              .forEach(
                  r -> {
                    r.getQuotations().add(savedQuotation);
                    RequestItem res = requestItemService.save(r);

                    supplierRequestMapRepository.updateDocumentStatus(
                        res.getId(), supplier.getId());
                  });
        });
  }


  @CacheEvict(value = {
          "allQuotations", "allQuotationsPage", "quotationById", "quotationBySupplier",
          "quotationNotLinkedToLpoWithRequestItems", "quotationUnderHodReviewWithRequestItems",
          "quotationsLinkedToLPOByDepartment", "quotationsLinkedToLpo", "approvedQuotationsBySupplier",
          "quotationsNonExpiredNotLinkedToLPO", "supplierQuotationDto"
  }, allEntries = true)
  public Quotation save(Quotation quotation) {
    return quotationRepository.save(quotation);
  }

  public List<Quotation> findQuotationNotExpiredAndNotLinkedToLpo() {
    return quotationRepository.findAllNonExpiredNotLinkedToLPO();
  }

  public List<Quotation> findQuotationLinkedToLPO() {
    return quotationRepository.findByLinkedToLpoTrue();
  }

  public List<Quotation> findByLinkedToLpoTrueAndHodReviewTrue() {
    return quotationRepository.findByLinkedToLpoTrueAndHodReviewTrue();
  }

  public List<Quotation> findQuotationsWithAuditorComments() {

    log.info("Fetch quotations with comments from auditor");
    return quotationRepository.findBQuotationsWithAuditorComment();
  }

  public List<QuotationAndRelatedRequestItemsDto> fetchQuotationLinkedToLpoWithRequestItems() {

    log.info("Find quotations not linked to lpo and attach related request items");
    List<Quotation> quotations = findQuotationLinkedToLPO();
    return pairQuotationsRelatedWithRequestItems(quotations);
  }
  private List<QuotationAndRelatedRequestItemsDto> pairQuotationsRelatedWithRequestItems(
          Collection<Quotation> quotations) {

    log.info("Attach quotation and their related request items");
    List<QuotationAndRelatedRequestItemsDto> data = new ArrayList<>();
    quotations.forEach(
            quotation -> {
              QuotationAndRelatedRequestItemsDto qri = new QuotationAndRelatedRequestItemsDto();
              qri.setQuotation(quotation);
              List<RequestItem> requestItems = requestItemService.findItemsUnderQuotation(quotation.getId());
              List<RequestItemDto> requestItemDTOList = new ArrayList<>();
              for (RequestItem requestItem : requestItems) {
                RequestItemDto requestItemDTO = RequestItemDto.toDto(requestItem);
                requestItemDTOList.add(requestItemDTO);
              }
              qri.setRequestItems(requestItemDTOList);
              data.add(qri);
            });
    return data;
  }

  @Cacheable(value = "quotationsLinkedToLPOByDepartment")
  public List<Quotation> findQuotationLinkedToLPOByDepartment() {

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    Department employeeDept =
        employeeService.findEmployeeByEmail(username).getDepartment();
    log.info(
        "Fetch quotations to be reviewed. User: {} in department: {}", username, employeeDept.getName());
    return quotationRepository.findByLinkedToLpoTrueAndDepartment(employeeDept.getId());
  }

  @Cacheable(value = "quotationsLinkedToLpo", key = "{#pageable.getPageNumber(), #pageable.getPageSize()}")
  public Page<Quotation> findAllQuotationsLinkedToLPO(Pageable pageable) {

    log.info("Fetch all quotations linked to LPO");
    return quotationRepository.findByLinkedToLpoTrue(pageable);
  }

  @Cacheable(value = "quotationBySupplier", key = "#supplierId")
  public List<Quotation> findBySupplier(int supplierId) {
    return quotationRepository.findBySupplierId(supplierId);
  }

  @Cacheable(value = "quotationsNonExpiredNotLinkedToLPO", key = "#requestItemIds")
  public List<Quotation> findNonExpiredNotLinkedToLPO(List<Integer> requestItemIds) {
    return quotationRepository.findNonExpiredNotLinkedToLPO(requestItemIds);
  }

  @Cacheable(value = "allQuotations")
  public List<Quotation> findAll() {
    return quotationRepository.findAll();
  }

  @Cacheable(value = "allQuotationsPage", key = "{#pageNo, #pageSize}")
  public Page<Quotation> findAll(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize);
    return quotationRepository.findAll(pageable);
  }

  @Transactional(rollbackFor = Exception.class, readOnly = true)
  public boolean existByQuotationId(int quotationId) {
    return quotationRepository.existsById(quotationId);
  }

  @CacheEvict(value = {
          "allQuotations", "allQuotationsPage", "quotationById", "quotationBySupplier",
          "quotationNotLinkedToLpoWithRequestItems", "quotationUnderHodReviewWithRequestItems",
          "quotationsLinkedToLPOByDepartment", "quotationsLinkedToLpo", "approvedQuotationsBySupplier",
          "quotationsNonExpiredNotLinkedToLPO", "supplierQuotationDto"
  }, allEntries = true)
  public void updateLinkedToLPO(int quotationId) {
    
      quotationRepository.updateLinkedToLPO(quotationId);
  }

  @Cacheable(value = "quotationById", key = "#quotationId")
  public Quotation findById(int quotationId) {

    return quotationRepository
        .findById(quotationId)
        .orElseThrow(() -> new QuotationNotFoundException(quotationId));
  }

  @Transactional(rollbackFor = Exception.class, readOnly = true)
  @CacheEvict(value = {
          "allQuotations", "allQuotationsPage", "quotationById", "quotationBySupplier",
          "quotationNotLinkedToLpoWithRequestItems", "quotationUnderHodReviewWithRequestItems",
          "quotationsLinkedToLPOByDepartment", "quotationsLinkedToLpo", "approvedQuotationsBySupplier",
          "quotationsNonExpiredNotLinkedToLPO", "supplierQuotationDto"
  }, allEntries = true)
  public List<RequestItem> assignToRequestItem(Set<RequestItem> requestItems, Set<Quotation> quotations) {

    List<RequestItem> assignedItems = requestItems.stream()
            .map(requestItem -> {
              requestItem.setQuotations(quotations);
              return requestItemService.save(requestItem);
            })
            .collect(Collectors.toList());
    sendAssignItemsEvent(assignedItems);
    return assignedItems;
  }

  private void sendAssignItemsEvent(List<RequestItem> assignedItems) {

    CompletableFuture.runAsync(() -> {
      AssignQuotationRequestItemEvent requestItemEvent =
              new AssignQuotationRequestItemEvent(this, assignedItems);
      applicationEventPublisher.publishEvent(requestItemEvent);
    });
  }

  @CacheEvict(value = {
          "allQuotations", "allQuotationsPage", "quotationById", "quotationBySupplier",
          "quotationNotLinkedToLpoWithRequestItems", "quotationUnderHodReviewWithRequestItems",
          "quotationsLinkedToLPOByDepartment", "quotationsLinkedToLpo",
          "quotationsNonExpiredNotLinkedToLPO", "supplierQuotationDto, approvedQuotationsBySupplier"
  }, allEntries = true)
  public Quotation assignRequestDocumentToQuotation(
      int quotationId, RequestDocument requestDocument) {

    Quotation quotation = findById(quotationId);
    quotation.setRequestDocument(requestDocument);
    return quotationRepository.save(quotation);
  }

  public long count() {
    return quotationRepository.countAll() + 1;
  }

  @Cacheable(value = "supplierQuotationDto", key = "#supplierId")
  public List<SupplierQuotationDto> findSupplierQuotation(int supplierId) {

    Supplier supplier =
        supplierRepository
            .findById(supplierId)
            .orElseThrow(() -> new SupplierNotFoundException(supplierId));

    List<Quotation> quotations = findBySupplier(supplier.getId());

    return quotations.stream()
        .map(
            x -> {
              SupplierQuotationDto sq = new SupplierQuotationDto();
              sq.setQuotation(x);
              List<RequestItem> requestItems =
                  requestItemService.findByQuotationId(x.getId());
              sq.setRequestItems(requestItems);
              return sq;
            })
        .collect(Collectors.toList());
  }


  @Cacheable(value = "quotationNotLinkedToLpoWithRequestItems")
  public List<QuotationAndRelatedRequestItemsDto> fetchQuotationNotLinkedToLpoWithRequestItems() {

    List<Quotation> quotationNotExpiredAndNotLinkedToLpo = findQuotationNotExpiredAndNotLinkedToLpo();
    return pairQuotationsRelatedWithRequestItems(quotationNotExpiredAndNotLinkedToLpo);
  }

  @Cacheable(value = "quotationUnderHodReviewWithRequestItems")
  public List<QuotationAndRelatedRequestItemsDto> fetchQuotationsUnderHodReviewWithRequestItems() {

    List<Quotation> quotationLinkedToLPOByDepartment = findQuotationLinkedToLPOByDepartment();
    quotationLinkedToLPOByDepartment.removeIf(Quotation::isHodReview);
    return pairQuotationsRelatedWithRequestItems(quotationLinkedToLPOByDepartment);
  }

  @Cacheable(value = "quotationUnderHodReviewWithRequestItems")
  public List<QuotationAndRelatedRequestItemsDto> fetchQuotationsUnderAuditorReviewWithRequestItems() {

    List<Quotation> quotationLinkedToLPO = findByLinkedToLpoTrueAndHodReviewTrue();
    quotationLinkedToLPO.removeIf(Quotation::isAuditorReview);
    return pairQuotationsRelatedWithRequestItems(quotationLinkedToLPO);
  }

  public List<QuotationAndRelatedRequestItemsDto> fetchQuotationsWithAuditorCommentWithRequestItems() {

    List<Quotation> quotationsWithAuditorComments = findQuotationsWithAuditorComments();
    return pairQuotationsRelatedWithRequestItems(quotationsWithAuditorComments);
  }

  @Cacheable(value = "approvedQuotationsBySupplier",
          key = "{#supplier.getName(), #pageable.getPageNumber(), #pageable.getPageSize()}")
  public Page<Quotation> findQuotationLinkedToLPOBySupplier(Supplier supplier, Pageable pageable) {

    log.info("Fetch approved quotations by supplier {}", supplier.getName());
    GenericSpecification<Quotation> specification = new GenericSpecification<>();
    specification.add(new SearchCriteria("linkedToLpo", true, SearchOperation.EQUAL));
    specification.add(new SearchCriteria("supplier", supplier, SearchOperation.EQUAL));
    return quotationRepository.findAll(specification, pageable);
  }

  @Transactional
  @CacheEvict(value = {
          "allQuotations", "allQuotationsPage", "quotationById", "quotationBySupplier",
          "quotationNotLinkedToLpoWithRequestItems", "quotationUnderHodReviewWithRequestItems",
          "quotationsLinkedToLPOByDepartment", "quotationsLinkedToLpo",
          "quotationsNonExpiredNotLinkedToLPO", "supplierQuotationDto, approvedQuotationsBySupplier",
          "approvedQuotationsBySupplier"
  }, allEntries = true)
  public List<QuotationMinorDto> approveByAuditor(Set<Integer> quotationIds, Employee auditor) {

    log.info("Approve quotations by auditor: {}", auditor.getEmail());
    int updatedCount = quotationRepository.approveQuotationsByAuditor(auditor, quotationIds);

    if (updatedCount == 0) {
      throw new NoQuotationsToUpdateException();
    }

    log.info("{}/{} submitted quotations were updated", updatedCount, quotationIds.size());
    CompletableFuture.runAsync(() -> sendApproveRequestItemsEmailToGM());
    return quotationRepository.findByIdIn(quotationIds)
            .stream()
            .map(QuotationMinorDto::toDto)
            .collect(Collectors.toList());
  }

  private void sendApproveRequestItemsEmailToGM() {

    log.info("Send approve request notification to General manager");
    Employee generalManager = employeeService.getGeneralManager();
    String message =
            MessageFormat.format(
                    "Dear {0}, Kindly check on request items ready for approval",
                    generalManager.getFullName());
    senderUtil.sendComposeAndSendEmail(
            "APPROVE REQUEST ITEMS",
            message,
            emailTemplate,
            EmailType.REQUEST_ITEM_APPROVAL_GM,
            generalManager.getEmail());
  }

}
