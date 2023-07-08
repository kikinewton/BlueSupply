package com.logistics.supply.exception;

public class PettyCashEndorsementException extends BadRequestException {

    public PettyCashEndorsementException(int pettyCashId) {
        super("Petty cash with id %s  can not be endorsed".formatted(pettyCashId), AppErrorCode.PETTY_CASH_REQUEST);
    }
}
