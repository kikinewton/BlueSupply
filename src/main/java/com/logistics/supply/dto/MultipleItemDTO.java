package com.logistics.supply.dto;

import com.logistics.supply.enums.RequestReason;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Size;
import java.util.List;

@Getter
@ToString
public class MultipleItemDTO {

    @Size(min = 1)
    private List<ReqItems> multipleRequestItem;


}
