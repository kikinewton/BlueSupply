package com.logistics.supply.enums;

public enum RequestStatus {
    PROCESSED("PROCESSED"), CANCELLED("CANCELLED"), PENDING("PENDING");

    private String requestStatus;

    RequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestStatus() {
        return requestStatus;
    }
}
