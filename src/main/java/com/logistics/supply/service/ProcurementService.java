package com.logistics.supply.service;

import com.logistics.supply.dto.ProcurementDTO;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import lombok.var;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ProcurementService extends AbstractDataService {

  public RequestItem assignProcurementDetails(RequestItem item, ProcurementDTO procurementDTO) {

    try {
      if (Objects.nonNull(item)) {
        item.setUnitPrice(procurementDTO.getUnitPrice());
        var amount = procurementDTO.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        System.out.println("amount: " + amount);
        item.setAmount(amount);
        Optional<Supplier> supplier =
            supplierRepository.findById(procurementDTO.getSupplier().getId());
        if (supplier.isPresent()) {
          System.out.println("Supplier: ======>> " + supplier.get() );
          item.setSupplier(supplier.get());
          return requestItemRepository.save(item);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
