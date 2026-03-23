package com.logistics.supply.interfaces.projections;

import java.math.BigDecimal;

public interface MonthlyTrendProjection {
    String getMonth();
    Long getRequestCount();
    BigDecimal getTotalValue();
}
