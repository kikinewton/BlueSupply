package com.logistics.supply.service;

import com.logistics.supply.dto.CreateQuotationRequest;
import com.logistics.supply.dto.RequestQuotationPair;
import com.logistics.supply.dto.SupplierQuotationDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.exception.QuotationNotFoundException;
import com.logistics.supply.exception.RequestDocumentNotFoundException;
import com.logistics.supply.exception.SupplierNotFoundException;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.*;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
  private final RequestItemRepository requestItemRepository;
  private final SupplierRequestMapRepository supplierRequestMapRepository;
  private final RequestDocumentRepository requestDocumentRepository;
  private final EmployeeRepository employeeRepository;

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
    Employee employee = employeeRepository.findByEmailAndEnabledIsTrue(username).get();
    quotation.setCreatedBy(employee);

    String ref = IdentifierUtil.idHandler("QUO", supplier.getName(), String.valueOf(count));
    quotation.setQuotationRef(ref);
    quotation.setRequestDocument(requestDocument);
    Quotation savedQuotation = save(quotation);
    CompletableFuture.runAsync(
        () -> {
          Set<RequestItem> requestItems =
              quotationRequest.getRequestItemIds().stream()
                  .map(x -> requestItemRepository.findById(x).get())
                  .collect(Collectors.toSet());
          requestItems.stream()
              .forEach(
                  r -> {
                    r.getQuotations().add(savedQuotation);
                    RequestItem res = requestItemRepository.save(r);

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

  public List<Quotation> findQuotationLinkedToLPOByDepartment() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    Department employeeDept =
        employeeRepository.findByEmailAndEnabledIsTrue(username).get().getDepartment();
    log.info(
        "Get quotations to be reviewed by: {} in department: {}", username, employeeDept.getName());
    return quotationRepository.findByLinkedToLpoTrueAndDepartment(employeeDept.getId());
  }

  public Page<Quotation> findAllQuotationsLinkedToLPO(int pageNo, int pageSize) {
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

  public List<RequestQuotationPair> test() {
    List<RequestQuotationPair> pairId = new ArrayList<>();
    try {
      pairId.addAll(quotationRepository.findQuotationRequestItemPairId());
      return pairId;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return pairId;
  }

  public List<Quotation> findAll() {
    return quotationRepository.findAll();
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

  @SneakyThrows
  @Transactional(readOnly = true)
  public Quotation findById(int quotationId) {

    return quotationRepository
        .findById(quotationId)
        .orElseThrow(() -> new QuotationNotFoundException(quotationId));
  }

  @Transactional(rollbackFor = Exception.class, readOnly = true)
  public RequestItem assignToRequestItem(RequestItem requestItem, Set<Quotation> quotations) {
    requestItem.setQuotations(quotations);
    return requestItemRepository.save(requestItem);
  }

  public Quotation assignRequestDocumentToQuotation(
      int quotationId, RequestDocument requestDocument) throws GeneralException {
    Optional<Quotation> quotation = quotationRepository.findById(quotationId);
    if (quotation.isPresent()) {
      Quotation q = quotation.get();
      q.setRequestDocument(requestDocument);
      try {
        return quotationRepository.save(q);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
    throw new GeneralException("ASSIGN DOCUMENT FAILED", HttpStatus.BAD_REQUEST);
  }

  public List<Quotation> assignDocumentToQuotationBySupplierId(
      int supplierId, RequestDocument requestDocument) throws GeneralException {
    try {
      List<Quotation> quotations = quotationRepository.findQuotationBySupplierId(supplierId);
      if (quotations.size() > 0) {
        List<Quotation> result =
            quotations.stream()
                .map(
                    q -> {
                      q.setRequestDocument(requestDocument);
                      return quotationRepository.save(q);
                    })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return result;
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    throw new GeneralException("ASSIGN DOCUMENT FAILED", HttpStatus.BAD_REQUEST);
  }

  public long count() {
    return quotationRepository.countAll() + 1;
  }

  public List<SupplierQuotationDTO> findSupplierQuotation(int supplierId) {

    Supplier supplier =
        supplierRepository
            .findById(supplierId)
            .orElseThrow(() -> new SupplierNotFoundException(supplierId));
    List<Quotation> quotations = findBySupplier(supplier.getId());
    List<SupplierQuotationDTO> result =
        quotations.stream()
            .map(
                x -> {
                  SupplierQuotationDTO sq = new SupplierQuotationDTO();
                  sq.setQuotation(x);
                  List<RequestItem> requestItems =
                      requestItemRepository.findByQuotationId(x.getId());
                  sq.setRequestItems(requestItems);
                  return sq;
                })
            .collect(Collectors.toList());
    return result;
  }

  public void reviewByHod(int quotationId) {
    quotationRepository.updateReviewStatus(quotationId);
  }
}
