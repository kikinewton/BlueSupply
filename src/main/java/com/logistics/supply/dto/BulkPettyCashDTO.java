package com.logistics.supply.dto;

import com.logistics.supply.model.PettyCash;
import lombok.Getter;

import java.util.Set;

@Getter
public class BulkPettyCashDTO {
    Set<PettyCash> pettyCash;
}
