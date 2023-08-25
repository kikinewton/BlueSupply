package com.logistics.supply.exception;

public class NoQuotationsToUpdateException extends BadRequestException {
    public NoQuotationsToUpdateException() {
        super("No quotations were updated because hod endorse is false for all IDs", AppErrorCode.QUOTATION);
    }
}
