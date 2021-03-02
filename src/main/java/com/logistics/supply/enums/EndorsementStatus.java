package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum EndorsementStatus {

    REJECTED("REJECTED"), ENDORSED("ENDORSED"), PENDING("PENDING");

    private String endorsementStatus;

    EndorsementStatus(String endorsementStatus) {
        this.endorsementStatus = endorsementStatus;
    }
}
