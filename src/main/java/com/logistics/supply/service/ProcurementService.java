package com.logistics.supply.service;

import com.logistics.supply.dto.ProcurementDTO;
import com.logistics.supply.dto.SetSupplierDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.RequestCategory;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
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
  public RequestItem assignMultipleSuppliers(RequestItem item, Set<Supplier> multipleSuppliers) {

    if (item.getEndorsement().equals(EndorsementStatus.ENDORSED)
        && item.getStatus().equals(RequestStatus.PENDING)) {
      Set<Supplier> suppliers =
          multipleSuppliers.stream()
              .filter(s -> supplierRepository.existsById(Objects.requireNonNull(s.getId())))
              .map(x -> supplierRepository.findById(x.getId()).get())
              .collect(Collectors.toSet());
      Set<Quotation> quotations =
          suppliers.stream()
              .map(
                  x -> {
                    Quotation q = new Quotation();
                    q.setSupplier(x);

                    Quotation result = quotationRepository.save(q);
                    return result;
                  })
              .collect(Collectors.toSet());
      item.setQuotations(quotations);

      return requestItemService.assignSuppliersToRequestItem(item, suppliers);
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public Set<RequestItem> assignDetailsForMultipleItems(SetSupplierDTO supplierDTO) {

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
                c ->
                    requestItemService.assignRequestCategory(
                        c.getId(), supplierDTO.getRequestCategory()))
            .collect(Collectors.toSet());

    if (items.size() > 0) return items;
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
