package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.LifecycleState;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Getter
public class RequestItemListDTO {
    @Size(min = 1)
    List<RequestItem> items;
    @FutureOrPresent
    Date deliveryDate;
    int quotationId;

}
