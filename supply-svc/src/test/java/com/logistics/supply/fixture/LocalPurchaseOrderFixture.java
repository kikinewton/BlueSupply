package com.logistics.supply.fixture;

import com.logistics.supply.model.LocalPurchaseOrder;
import com.logistics.supply.model.RequestItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.Set;

public class LocalPurchaseOrderFixture {

    private LocalPurchaseOrderFixture() {}

    public static LocalPurchaseOrderTestBuilder approved(RequestItem... requestItems) {
        return new LocalPurchaseOrderTestBuilder().approved(requestItems);
    }

    @Getter
    @With
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocalPurchaseOrderTestBuilder {
        private Integer        supplierId   = 1;
        private Boolean        isApproved   = false;
        private Set<RequestItem> requestItems = Set.of();

        public LocalPurchaseOrderTestBuilder approved(RequestItem... items) {
            this.isApproved   = true;
            this.requestItems = Set.of(items);
            return this;
        }

        public LocalPurchaseOrder build() {
            LocalPurchaseOrder lpo = new LocalPurchaseOrder();
            lpo.setSupplierId(supplierId);
            lpo.setIsApproved(isApproved);
            lpo.setRequestItems(requestItems);
            return lpo;
        }
    }
}
