package com.logistics.supply.interfaces.projections;

public interface CancellationRateProjection {
    String getDepartment();
    Long getTotalRequests();
    Long getCancelledCount();
}
