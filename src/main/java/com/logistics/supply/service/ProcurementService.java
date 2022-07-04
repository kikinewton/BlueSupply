package com.logistics.supply.service;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProcurementService {
  private final RequestItemService requestItemService;

  @Transactional(rollbackFor = Exception.class)
  public Set<RequestItem> assignRequestToSupplier(
      Set<Supplier> suppliers, Set<RequestItem> requestItems) {
    requestItems.removeIf(
        r ->
            !EndorsementStatus.REJECTED.equals(r.getEndorsement())
                && !RequestStatus.PENDING.equals(r.getStatus()));

    Set<RequestItem> finalRequest =
            requestItems.stream()
            .map(x -> requestItemService.assignSuppliersToRequestItem(x, suppliers))
            .collect(Collectors.toSet());
    return finalRequest;
  }
}
