package com.logistics.supply.exception;

public class PettyCashFundsAllocationException extends BadRequestException {
    public PettyCashFundsAllocationException(int pettyCashId) {
        super("Petty cash with pettyCashId %s can not be funded".formatted(pettyCashId), AppErrorCode.PETTY_CASH_REQUEST);
    }
}
