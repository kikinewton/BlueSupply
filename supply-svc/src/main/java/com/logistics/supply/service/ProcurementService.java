package com.logistics.supply.service;

import com.logistics.supply.dto.MappingSuppliersAndRequestItemsDto;
import com.logistics.supply.dto.RequestItemDto;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ProcurementService {

  private final RequestItemService requestItemService;

  private final SupplierService supplierService;

  @Transactional(rollbackFor = Exception.class)
  public Set<RequestItemDto> assignRequestToSupplier( MappingSuppliersAndRequestItemsDto mappingDTO) {

      log.info("Assign suppliers to request items");
      Set<RequestItem> requestItems =
              mappingDTO.getRequestItems().stream()
                      .filter(i -> requestItemService.existById(i.getId()))
                      .map(r -> requestItemService.findById(r.getId()))
                      .collect(Collectors.toSet());

      Set<Supplier> suppliers =
              mappingDTO.getSuppliers().stream()
                      .map(s -> supplierService.findById(s.getId()))
                      .collect(Collectors.toSet());


    requestItems.removeIf(
        r ->
            !EndorsementStatus.REJECTED.equals(r.getEndorsement())
                && !RequestStatus.PENDING.equals(r.getStatus()));

    return requestItems.stream()
            .map(x -> requestItemService.assignSuppliersToRequestItem(x, suppliers))
            .map(RequestItemDto::toDto)
            .collect(Collectors.toSet());
  }

    public Set<RequestItem> findUnprocessedRequestItemsForSupplier(int supplierId) {
        return requestItemService.findUnprocessedRequestItemsForSupplier(supplierId);
    }

    public List<RequestItem> getEndorsedItemsWithAssignedSuppliers() {
        return requestItemService.getEndorsedItemsWithAssignedSuppliers();
    }

    public File generateRequestListForSupplier(int supplierId) {
        return requestItemService.generateRequestListForSupplier(supplierId);
    }
}
