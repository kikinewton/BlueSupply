package com.logistics.supply.service;

import com.logistics.supply.dto.ProcurementDTO;
import com.logistics.supply.model.RequestItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class ProcurementService extends AbstractDataService {

  public RequestItem assignProcurementDetails(RequestItem item, ProcurementDTO procurementDTO) {

    try {
      if (Objects.nonNull(item)) {
        item.setUnitPrice(procurementDTO.getUnitPrice());
        var amount = procurementDTO.getUnitPrice() * item.getQuantity();
        System.out.println("amount: " + amount);
        item.setAmount(amount);
        item.setSupplier(procurementDTO.getSupplier());
        return requestItemRepository.save(item);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return item;
  }
}
