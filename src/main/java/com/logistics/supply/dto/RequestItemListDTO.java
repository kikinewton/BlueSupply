package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import lombok.Data;
import org.apache.catalina.LifecycleState;

import java.util.List;

@Data
public class RequestItemListDTO {
    List<RequestItem> items;
}
