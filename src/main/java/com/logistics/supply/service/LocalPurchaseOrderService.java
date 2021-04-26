package com.logistics.supply.service;

import com.logistics.supply.model.LocalPurchaseOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LocalPurchaseOrderService extends AbstractDataService {

    public LocalPurchaseOrder saveLPO(LocalPurchaseOrder lpo) {
        try {
            return localPurchaseOrderRepository.save(lpo);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<LocalPurchaseOrder> findAll() {
        List<LocalPurchaseOrder> lpos = new ArrayList<>();
        try {
            lpos.addAll(localPurchaseOrderRepository.findAll());
            return lpos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lpos;
    }

    public LocalPurchaseOrder findLpoById(int lpoId) {
        try {
            return localPurchaseOrderRepository.findById(lpoId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<LocalPurchaseOrder> findLpoBySupplier(int supplierId) {
        List<LocalPurchaseOrder> lpos = new ArrayList<>();
        try {
             lpos.addAll(localPurchaseOrderRepository.findBySupplierId(supplierId));
             return lpos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lpos;
    }

}
