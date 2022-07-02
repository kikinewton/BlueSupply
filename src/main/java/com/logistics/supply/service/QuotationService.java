package com.logistics.supply.service;

import com.logistics.supply.dto.RequestQuotationDTO;
import com.logistics.supply.dto.RequestQuotationPair;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.repository.QuotationRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.QUOTATION_NOT_FOUND;

@Service
@Slf4j
public class QuotationService {

  @Autowired QuotationRepository quotationRepository;
  @Autowired RequestItemService requestItemService;

  public Quotation save(Quotation quotation) {
    return quotationRepository.save(quotation);
  }

  public List<Quotation> findQuotationNotExpiredAndNotLinkedToLpo() {
    return quotationRepository.findAllNonExpiredNotLinkedToLPO();
  }

  public List<Quotation> findQuotationLinkedToLPO() {
    return quotationRepository.findByLinkedToLpoTrue();
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
      pairId.forEach(System.out::println);
      return pairId;
    } catch (Exception e) {
      log.error(e.toString());
    }
    return pairId;
  }

  public List<RequestQuotationDTO> findQuotationsWithoutAssignedDocument() {
    List<RequestQuotationDTO> requestQuotations = new ArrayList<>();
    List<Quotation> quotations = new ArrayList<>();
    try {
      List<RequestItem> items =
          requestItemService.findRequestItemsWithoutDocInQuotation().stream()
              .filter(Objects::nonNull)
              .collect(Collectors.toList());

      quotations.addAll(quotationRepository.findQuotationWithoutDocument());
      requestQuotations =
          quotations.stream()
              .filter(Objects::nonNull)
              .map(
                  i -> {
                    RequestQuotationDTO rq = new RequestQuotationDTO();
                    for (RequestItem r : items) {
                      if (r.getQuotations().contains(i)) {
                        if (Objects.nonNull(i)) rq.setQuotation(i);
                        if (Objects.nonNull(r.getName())) rq.setName(r.getName());
                        if (Objects.nonNull(r.getQuantity())) rq.setQuantity(r.getQuantity());
                        if (Objects.nonNull(r.getRequestDate()))
                          rq.setRequestDate(r.getRequestDate());
                      }
                    }
                    return rq;
                  })
              .filter(k -> Objects.nonNull(k))
              .collect(Collectors.toList());

      return requestQuotations;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return requestQuotations;
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
        .orElseThrow(() -> new GeneralException(QUOTATION_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @Transactional(rollbackFor = Exception.class, readOnly = true)
  public RequestItem assignToRequestItem(RequestItem requestItem, Set<Quotation> quotations) {
    requestItem.setQuotations(quotations);
    return requestItemService.saveRequestItem(requestItem);
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
}
