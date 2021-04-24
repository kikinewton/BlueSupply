package com.logistics.supply.service;

import com.logistics.supply.dto.MapQuotationsToRequestItemsDTO;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestItem;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuotationService extends AbstractDataService {

  public Quotation save(Quotation quotation) {
    try {
      return quotationRepository.save(quotation);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }



  public List<Quotation> findBySupplier(int supplierId) {
    List<Quotation> quotations = new ArrayList<>();
    try {
      quotations.addAll(quotationRepository.findBySupplierId(supplierId));
      return quotations;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return quotations;
  }

  public Quotation findByRequestDocumentId(int requestDocumentId) {
    try {
      return quotationRepository.findByRequestDocumentId(requestDocumentId);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<Quotation> findAll() {
    try {
      return quotationRepository.findAll();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem assignToRequestItem(RequestItem requestItem, Set<Quotation> quotations) {
    requestItem.setQuotations(quotations);
    return requestItemRepository.save(requestItem);
  }
}
