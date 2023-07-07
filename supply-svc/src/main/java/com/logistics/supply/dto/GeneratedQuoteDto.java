package com.logistics.supply.dto;

import com.logistics.supply.model.Supplier;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GeneratedQuoteDto {

    @NotNull
    private Supplier supplier;
    private String phoneNo;
    private String location;
    private String deliveryDate;
    @NotBlank
    private String productDescription;
    private List<ItemUpdateDto> items;

}
