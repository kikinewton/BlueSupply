package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import lombok.Getter;

import java.util.List;

@Getter
public class CancelRequestDTO {
    int employeeId;
    List<RequestItem> cancelList;
}
