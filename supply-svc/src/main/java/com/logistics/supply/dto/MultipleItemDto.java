package com.logistics.supply.dto;

import lombok.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
