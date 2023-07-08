package com.logistics.supply.exception;

public class PettyCashApprovalException extends BadRequestException {

    public PettyCashApprovalException(int pettyCashId) {
        super("Petty cash with id %s can not be approved".formatted(pettyCashId), AppErrorCode.PETTY_CASH_REQUEST);
    }
}
