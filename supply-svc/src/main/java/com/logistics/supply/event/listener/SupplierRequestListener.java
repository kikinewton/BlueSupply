package com.logistics.supply.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;

import javax.persistence.PostPersist;
import com.logistics.supply.model.RequestForQuotation;
import com.logistics.supply.model.SupplierRequestMap;
import com.logistics.supply.repository.RequestForQuotationRepository;

@Slf4j
public class SupplierRequestListener {

    private final RequestForQuotationRepository requestForQuotationRepository;

  /**
   * Use lazy initialization to solve cyclic dependencies. For that, you need to create the
   * constructor yourself to inject spring bean and use @Lazy
   * (org.springframework.context.annotation.Lazy)
   *
   * @param requestForQuotationRepository
   */
  public SupplierRequestListener(
      @Lazy RequestForQuotationRepository requestForQuotationRepository) {
        this.requestForQuotationRepository = requestForQuotationRepository;
    }

    @PostPersist
    public void addRFQ(SupplierRequestMap supplierRequestMap) {
        log.info("==== add to RFQ ====");
        RequestForQuotation rfq = new RequestForQuotation();
        rfq.setSupplierRequestMap(supplierRequestMap);
        rfq.setSupplier(supplierRequestMap.getSupplier());
        requestForQuotationRepository.save(rfq);
    }


}
