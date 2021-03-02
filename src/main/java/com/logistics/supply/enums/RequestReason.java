package com.logistics.supply.enums;

public enum RequestReason {

    Replace("REPLACE"), Restock("RESTOCK"), FreshNeed("FRESH-NEED");

    private String requestReason;

    RequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public String getRequestReason() {
        return requestReason;
    }
}
