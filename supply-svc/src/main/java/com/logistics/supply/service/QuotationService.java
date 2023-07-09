package com.logistics.supply.service;

import com.logistics.supply.dto.*;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.AssignQuotationRequestItemEvent;
import com.logistics.supply.exception.QuotationNotFoundException;
import com.logistics.supply.exception.RequestDocumentNotFoundException;
import com.logistics.supply.exception.SupplierNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.QuotationRepository;
import com.logistics.supply.repository.RequestDocumentRepository;
import com.logistics.supply.repository.SupplierRepository;
import com.logistics.supply.repository.SupplierRequestMapRepository;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuotationService {

  private final QuotationRepository quotationRepository;
  private final SupplierRepository supplierRepository;
  private final RequestItemService requestItemService;
  private final SupplierRequestMapRepository supplierRequestMapRepository;
  private final RequestDocumentRepository requestDocumentRepository;
  private final EmployeeService employeeService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Transactional
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
    return savedQuotation;
  }

  public Quotation save(Quotation quotation) {
    return quotationRepository.save(quotation);
  }

  public List<Quotation> findQuotationNotExpiredAndNotLinkedToLpo() {
    return quotationRepository.findAllNonExpiredNotLinkedToLPO();
  }

  public List<Quotation> findQuotationLinkedToLPO() {
    return quotationRepository.findByLinkedToLpoTrue();
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

  public List<Quotation> findQuotationLinkedToLPOByDepartment() {

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    Department employeeDept =
        employeeService.findEmployeeByEmail(username).getDepartment();
    log.info(
        "Fetch quotations to be reviewed. User: {} in department: {}", username, employeeDept.getName());
    return quotationRepository.findByLinkedToLpoTrueAndDepartment(employeeDept.getId());
  }

  public Page<Quotation> findAllQuotationsLinkedToLPO(int pageNo, int pageSize) {

    log.info("Fetch all quotations linked to LPO");
    Pageable pageable = PageRequest.of(pageNo, pageSize);
    return quotationRepository.findByLinkedToLpoTrue(pageable);
  }

  public List<Quotation> findBySupplier(int supplierId) {
    return quotationRepository.findBySupplierId(supplierId);
  }

  public List<Quotation> findNonExpiredNotLinkedToLPO(List<Integer> requestItemIds) {
    return quotationRepository.findNonExpiredNotLinkedToLPO(requestItemIds);
  }

  public Quotation findByRequestDocumentId(int requestDocumentId) {
    return quotationRepository.findByRequestDocumentId(requestDocumentId);
  }

  public List<RequestQuotationPair> fetchRequestQuotationPair() {
   return quotationRepository.findQuotationRequestItemPairId();
  }

  public List<Quotation> findAll() {
    return quotationRepository.findAll();
  }

  public Page<Quotation> findAll(int pageNo, int pageSize) {
    Pageable pageable = PageRequest.of(pageNo, pageSize);
    return quotationRepository.findAll(pageable);
  }

  @Transactional(rollbackFor = Exception.class, readOnly = true)
  public boolean existByQuotationId(int quotationId) {
    return quotationRepository.existsById(quotationId);
  }

  public void updateLinkedToLPO(int quotationId) {
    try {
      quotationRepository.updateLinkedToLPO(quotationId);
    } catch (Exception e) {
      log.error(e.toString());
    }
  }

  public boolean expireQuotation(int quotationId) {
    Optional<Quotation> q = quotationRepository.findById(quotationId);
    if (q.isPresent()) {
      Optional<Quotation> result =
          q.map(
              x -> {
                x.setExpired(true);
                try {
                  return quotationRepository.save(x);
                } catch (Exception e) {
                  log.error(e.toString());
                }
                return null;
              });
      if (result.isPresent()) {
        return true;
      }
    }
    return false;
  }


  @Transactional(readOnly = true)
  public Quotation findById(int quotationId) {

    return quotationRepository
        .findById(quotationId)
        .orElseThrow(() -> new QuotationNotFoundException(quotationId));
  }

  @Transactional(rollbackFor = Exception.class, readOnly = true)
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

  public Quotation assignRequestDocumentToQuotation(
      int quotationId, RequestDocument requestDocument) {
    Quotation quotation = quotationRepository.findById(quotationId)
            .orElseThrow(() -> new QuotationNotFoundException(quotationId));
    quotation.setRequestDocument(requestDocument);
    return quotationRepository.save(quotation);
  }

  public List<Quotation> assignDocumentToQuotationBySupplierId(
      int supplierId, RequestDocument requestDocument) throws GeneralException {
    try {
      List<Quotation> quotations = quotationRepository.findQuotationBySupplierId(supplierId);
      if (!quotations.isEmpty()) {
        return quotations.stream()
                .map(
                    q -> {
                      q.setRequestDocument(requestDocument);
                      return quotationRepository.save(q);
                    })
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException("ASSIGN DOCUMENT FAILED", HttpStatus.BAD_REQUEST);
  }

  public long count() {
    return quotationRepository.countAll() + 1;
  }

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

  public void reviewByHod(int quotationId) {
    quotationRepository.updateReviewStatus(quotationId);
  }

  public List<QuotationAndRelatedRequestItemsDto> fetchQuotationNotLinkedToLpoWithRequestItems() {
    List<Quotation> quotationNotExpiredAndNotLinkedToLpo = findQuotationNotExpiredAndNotLinkedToLpo();
    return pairQuotationsRelatedWithRequestItems(quotationNotExpiredAndNotLinkedToLpo);
  }

  public List<QuotationAndRelatedRequestItemsDto> fetchQuotationUnderReviewWithRequestItems() {
    List<Quotation> quotationLinkedToLPOByDepartment = findQuotationLinkedToLPOByDepartment();
    quotationLinkedToLPOByDepartment.removeIf(Quotation::isReviewed);
    return pairQuotationsRelatedWithRequestItems(quotationLinkedToLPOByDepartment);
  }
}
