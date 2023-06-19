package com.logistics.supply.exception;

public enum AppErrorCode {


    LPO_REQUEST(100),
    FLOAT_REQUEST(200),
    PETTY_CASH_REQUEST(300),
    QUOTATION(400),
    REQUEST_DOCUMENT(500),
    EMPLOYEE(600),
    VERIFICATION_TOKEN(700);


    private final int value;

    AppErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
