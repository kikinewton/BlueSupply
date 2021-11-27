package com.logistics.supply.event.listener;

import com.logistics.supply.model.LocalPurchaseOrderDraft;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.QuotationService;
import com.logistics.supply.service.RequestItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;

import javax.persistence.PostPersist;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class LpoDraftEventListener {

  private final QuotationService quotationService;
  private final RequestItemService requestItemService;

  public LpoDraftEventListener(@Lazy QuotationService quotationService, @Lazy RequestItemService requestItemService) {
    this.quotationService = quotationService;
    this.requestItemService = requestItemService;
  }

  @Async
  @PostPersist
  public void expireQuotations(LocalPurchaseOrderDraft lpo) {
    try {
      /** Flag quotation related to lpo as linked */

      quotationService.updateLinkedToLPO(lpo.getQuotation().getId());

      /**
       * Loop through all quotations related to request items in lpo and invalidate(expire = true)
       * if all the request items associated to that quotations have unit price
       */

      List<Integer> requestItemIds =
          lpo.getRequestItems().stream().map(RequestItem::getId).collect(Collectors.toList());
      requestItemIds.forEach(System.out::println);
      if (requestItemService.priceNotAssigned(requestItemIds)) {
        log.info("=== Some items have not been assigned a final supplier ===");
      } else {
        log.info("=== All items been assigned a final supplier, expire related quotations ===");
        List<Quotation> l =
            quotationService.findNonExpiredNotLinkedToLPO(requestItemIds).stream()
                .map(
                    q -> {
                      q.setExpired(true);
                      return quotationService.save(q);
                    })
                .collect(Collectors.toList());
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
  }
}
