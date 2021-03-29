package com.logistics.supply.dto;

import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class MultipleEndorsementDTO {

    private Integer employee_id;
    private List<RequestItem> endorsedList;
}
