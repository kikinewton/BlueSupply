package com.logistics.supply.exception;

public class RequestItemStatusException extends BadRequestException {

    public RequestItemStatusException(String message) {
        super(message, AppErrorCode.LPO_REQUEST);
    }
}
