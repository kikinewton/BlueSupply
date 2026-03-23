package com.logistics.supply.interfaces.projections;

public interface CycleTimeProjection {
    String getDepartment();
    Integer getAvgDaysToEndorsement();
    Integer getAvgDaysToApproval();
}
