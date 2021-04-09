package com.logistics.supply.service;

import com.logistics.supply.dto.ProcurementDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import lombok.var;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProcurementService extends AbstractDataService {

  public RequestItem assignProcurementDetails(RequestItem item, ProcurementDTO procurementDTO) {

    try {
      if (Objects.nonNull(item)) {
        item.setUnitPrice(procurementDTO.getUnitPrice());
        var amount = procurementDTO.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        System.out.println("amount: " + amount);
        item.setTotalPrice(amount);
        Optional<Supplier> supplier =
            supplierRepository.findById(procurementDTO.getSupplier().getId());
        if (supplier.isPresent()) {
          System.out.println("Supplier: ======>> " + supplier.get());
          item.setSuppliedBy(supplier.get().getId());
          return requestItemRepository.save(item);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public RequestItem assignMultipleSuppliers(RequestItem item, Set<Supplier> multipleSuppliers) {
    if (item.getEndorsement().equals(EndorsementStatus.ENDORSED)
        && item.getStatus().equals(RequestStatus.PENDING)) {
      Set<Supplier> suppliers =
          multipleSuppliers.stream()
              .filter(s -> supplierRepository.existsById(s.getId()))
              .map(x -> supplierRepository.findById(x.getId()).get())
              .collect(Collectors.toSet());
      item.setSuppliers(suppliers);
      return requestItemRepository.save(item);
    }
    return null;
  }

  public Set<RequestItem> attachQuotations() {
    return null;
  }
}
