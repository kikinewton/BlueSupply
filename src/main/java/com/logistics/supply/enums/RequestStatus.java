package com.logistics.supply.enums;

public enum RequestStatus {
    Processed("PROCESSED"), Cancelled("CANCELLED"), Pending("PENDING");

    private String requestStatus;

    RequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestStatus() {
        return requestStatus;
    }
}
