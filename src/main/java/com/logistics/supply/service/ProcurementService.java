package com.logistics.supply.service;

import com.logistics.supply.dto.ProcurementDTO;
import com.logistics.supply.model.RequestItem;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class ProcurementService extends AbstractDataService {

    public RequestItem assignProcurementDetails(int requestItemId, ProcurementDTO procurementDTO) {
        RequestItem item = requestItemRepository.findById(requestItemId).get();

        try {
            if (Objects.nonNull(item)) {
                item.setUnitPrice(procurementDTO.getUnitPrice());
                var amount = procurementDTO.getUnitPrice() * item.getUnitPrice();
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
