package com.logistics.supply.dto;

import lombok.Getter;
import lombok.ToString;

import com.logistics.supply.model.RequestItem;
import java.util.List;

@Getter
@ToString
public class MultipleRequestItemDTO {
    private List<RequestItem> requestList;
}
