package com.logistics.supply.service;

import com.logistics.supply.dto.RequestItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProcurementService {
  private final RequestItemService requestItemService;

  @Transactional(rollbackFor = Exception.class)
  public Set<RequestItemDto> assignRequestToSupplier(
      Set<Supplier> suppliers, Set<RequestItem> requestItems) {
    requestItems.removeIf(
        r ->
            !EndorsementStatus.REJECTED.equals(r.getEndorsement())
                && !RequestStatus.PENDING.equals(r.getStatus()));

    Set<RequestItemDto> finalRequest =
        requestItems.stream()
            .map(x -> requestItemService.assignSuppliersToRequestItem(x, suppliers))
            .map(RequestItemDto::toDto)
            .collect(Collectors.toSet());

    return finalRequest;
  }
}
