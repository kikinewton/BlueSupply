package com.logistics.supply.enums;

import lombok.Getter;

@Getter
public enum VerificationType {
    PASSWORD_RESET("PASSWORD_RESET");

    private String verificationType;

    VerificationType(String verificationType) {
        this.verificationType = verificationType;
    }
}
