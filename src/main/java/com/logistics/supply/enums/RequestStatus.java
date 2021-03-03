package com.logistics.supply.enums;

public enum RequestStatus {
    PROCESSED("PROCESSED"), ENDORSEMENT_CANCELLED("ENDORSEMENT_CANCELLED"), APPROVAL_CANCELLED("APPROVAL_CANCELLED"), PENDING("PENDING");

    private String requestStatus;

    RequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestStatus() {
        return requestStatus;
    }
}
