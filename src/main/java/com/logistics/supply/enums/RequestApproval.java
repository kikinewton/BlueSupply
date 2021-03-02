package com.logistics.supply.enums;

public enum RequestApproval {

    Approve("APPROVE"), Cancel("CANCEL"), Pending("PENDING");

    private String approvalStatus;

    RequestApproval(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }
}
