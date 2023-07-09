package com.logistics.supply.dto;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MultipleItemDto {

    @Size(min = 1)
    @NotNull
    private List<@Valid LpoMinorRequestItem> multipleRequestItem;

}
