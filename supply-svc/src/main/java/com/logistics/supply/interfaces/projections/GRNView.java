package com.logistics.supply.interfaces.projections;

import java.util.Date;

public interface GRNView {
    int getId();
    String getSupplier();
    String getGrnRef();
    String getCreatedBy();
    Date getPaymentDate();
    Date getCreatedDate();
    Date getDateOfApprovalHod();

}
