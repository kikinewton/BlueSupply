package com.logistics.supply.factory;

import com.logistics.supply.dto.FloatDto;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.model.Floats;

import java.util.Set;
import java.util.stream.Collectors;

public class FloatOrderFactory {

     FloatOrderFactory() {
    }

    public static Set<Floats> mapBulkFloatsFromFloatOrder(Set<FloatDto> floatItems, FloatOrder floatOrder) {

        return floatItems.stream()
                .map(
                        floatItem -> {
                            Floats floats = new Floats();
                            floats.setDepartment(floatOrder.getDepartment());
                            floats.setEstimatedUnitPrice(floatItem.getEstimatedUnitPrice());
                            floats.setItemDescription(floatItem.getItemDescription());
                            floats.setQuantity(floatItem.getQuantity());
                            floats.setFloatOrder(floatOrder);
                            floats.setFloatRef(floatOrder.getFloatOrderRef());
                            floats.setCreatedBy(floatOrder.getCreatedBy());
                            return floats;
                        })
                .collect(Collectors.toSet());
    }
}
