package com.logistics.supply.dto;

import com.logistics.supply.model.PettyCash;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class BulkPettyCashDTO {

    @Size(min = 1)
    Set<PettyCash> pettyCash;
}
