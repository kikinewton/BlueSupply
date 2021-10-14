package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum RequestApproval {

    APPROVED("APPROVED"), REJECTED("REJECTED"), PENDING("PENDING"), COMMENT("COMMENT");

    private String approvalStatus;

    RequestApproval(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

}
