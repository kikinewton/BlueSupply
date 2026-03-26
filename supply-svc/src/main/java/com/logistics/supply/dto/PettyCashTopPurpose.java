package com.logistics.supply.dto;

import java.math.BigDecimal;

public interface PettyCashTopPurpose {
    String getPurpose();
    int getRequestCount();
    BigDecimal getTotalSpend();
}
