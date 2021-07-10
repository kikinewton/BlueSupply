package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.LifecycleState;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class RequestItemListDTO {
    List<RequestItem> items;
    Date deliveryDate;
}
