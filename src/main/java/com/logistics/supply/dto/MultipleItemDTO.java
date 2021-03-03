package com.logistics.supply.dto;

import com.logistics.supply.enums.RequestReason;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class MultipleItemDTO {

    private Integer employee_id;
    private List<ReqItems> multipleRequestItem;


}
