package com.logistics.supply.service;

import com.logistics.supply.model.Quotation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
      quotationRepository.findBySupplierOrderByCreatedDateDesc(supplierId).addAll(quotations);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return quotations;
  }
}
