package com.logistics.supply.service;

import com.logistics.supply.dto.ProcurementDTO;
import com.logistics.supply.dto.SetSupplierDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.*;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProcurementService extends AbstractDataService {

  @Autowired private RequestItemService requestItemService;
  @Autowired private LocalPurchaseOrderService localPurchaseOrderService;

  @Transactional(rollbackFor = Exception.class)
  public RequestItem assignProcurementDetails(RequestItem item, ProcurementDTO procurementDTO) {

    try {
      RequestItem requestItem = requestItemService.findById(item.getId()).get();
      if (Objects.nonNull(requestItem)) {
        requestItem.setUnitPrice(procurementDTO.getUnitPrice());

        BigDecimal amount =
            procurementDTO.getUnitPrice().multiply(BigDecimal.valueOf(requestItem.getQuantity()));

        requestItem.setTotalPrice(amount);
        requestItem = requestItemRepository.save(requestItem);
        Optional<Supplier> supplier =
            supplierRepository.findById(procurementDTO.getSupplier().getId());
        if (supplier.isPresent()) {

          requestItem.setSuppliedBy(supplier.get().getId());
          requestItemRepository.assignFinalSupplier(supplier.get().getId(), requestItem.getId());
          return requestItemRepository.findById(requestItem.getId()).get();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  @Transactional(rollbackFor = Exception.class)
  public Set<RequestItem> assignRequestToSupplier(
      Set<Supplier> suppliers, Set<RequestItem> requestItems) {
    Set<RequestItem> requests =
        requestItems.stream()
            .map(
                item -> {
                  if (item.getEndorsement().equals(EndorsementStatus.ENDORSED)
                      && item.getStatus().equals(RequestStatus.PENDING)) {
                    return item;
                  }
                  return null;
                })
            .filter(i -> Objects.nonNull(i))
            .collect(Collectors.toSet());

    System.out.println("requests = " + requests.size());

    Set<RequestItem> finalRequest =
        requests.stream()
            .map(
                x -> {
                  return requestItemService.assignSuppliersToRequestItem(x, suppliers);
                })
            .collect(Collectors.toSet());
    System.out.println("finalRequest = " + finalRequest.size());

    if (finalRequest.size() > 0) return finalRequest;
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public LocalPurchaseOrder assignDetailsForMultipleItems(SetSupplierDTO supplierDTO) {

    var items =
        supplierDTO.getItemAndUnitPrice().stream()
            .filter(
                x ->
                    requestItemService.supplierIsPresent(
                        x.getRequestItem(), supplierDTO.getSupplier()))
            .peek(System.out::println)
            .map(
                y -> {
                  ProcurementDTO dto =
                      new ProcurementDTO(y.getUnitPrice(), supplierDTO.getSupplier());
                  return assignProcurementDetails(y.getRequestItem(), dto);
                })
            .map(
                z ->
                    requestItemService.assignRequestCategory(
                        z.getId(), supplierDTO.getRequestCategory()))
            .collect(Collectors.toSet());

    LocalPurchaseOrder lpo = new LocalPurchaseOrder();
    lpo.setComment("");
    lpo.setRequestItems(items);
    lpo.setSupplierId(supplierDTO.getSupplier().getId());
    LocalPurchaseOrder newLpo = localPurchaseOrderService.saveLPO(lpo);

    if (Objects.nonNull(newLpo)) return newLpo;
    return null;
  }

  public List<RequestItem> getRequestItemsBySupplierId(int supplierId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getRequestItemsBySupplierId(supplierId));
      return items;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return items;
  }


}
