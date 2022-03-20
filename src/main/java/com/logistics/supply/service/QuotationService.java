package com.logistics.supply.service;

import com.logistics.supply.dto.RequestQuotationDTO;
import com.logistics.supply.dto.RequestQuotationPair;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestDocument;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.repository.QuotationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuotationService {

  @Autowired QuotationRepository quotationRepository;
  @Autowired RequestItemService requestItemService;

  public Quotation save(Quotation quotation) {
    try {
      return quotationRepository.save(quotation);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }


  public List<Quotation> findQuotationNotExpiredAndNotLinkedToLpo(){
    try {
      return quotationRepository.findAllNonExpiredNotLinkedToLPO();
    }
    catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }


  public List<Quotation> findQuotationLinkedToLPO() {
    try {
      return quotationRepository.findByLinkedToLpoTrue();
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
  }

  public List<Quotation> findBySupplier(int supplierId) {
    List<Quotation> quotations = new ArrayList<>();
    try {
      quotations.addAll(quotationRepository.findBySupplierId(supplierId));
      return quotations;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return quotations;
  }

  public List<Quotation> findNonExpiredNotLinkedToLPO(List<Integer> requestItemIds) {
    return quotationRepository.findNonExpiredNotLinkedToLPO(requestItemIds);
  }

  public Quotation findByRequestDocumentId(int requestDocumentId) {
    try {
      return quotationRepository.findByRequestDocumentId(requestDocumentId);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
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

  @Transactional(readOnly = true)
  public Quotation findById(int quotationId) {
    try {
      Optional<Quotation> quotation = quotationRepository.findById(quotationId);
      if (quotation.isPresent()) return quotation.get();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class, readOnly = true)
  public RequestItem assignToRequestItem(RequestItem requestItem, Set<Quotation> quotations) {
    requestItem.setQuotations(quotations);
    return requestItemService.saveRequestItem(requestItem);
  }

  public Quotation assignRequestDocumentToQuotation(
      int quotationId, RequestDocument requestDocument) {
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
    return null;
  }

  public List<Quotation> assignDocumentToQuotationBySupplierId(
      int supplierId, RequestDocument requestDocument) {
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
    return null;
  }

  public long count() {
    return quotationRepository.count() + 1;
  }
}
