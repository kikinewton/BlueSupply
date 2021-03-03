package com.logistics.supply.enums;

public enum RequestApproval {

    APPROVED("APPROVED"), REJECTED("REJECTED"), PENDING("PENDING"), NONE("NONE");

    private String approvalStatus;

    RequestApproval(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }
}
