package com.logistics.supply.exception;

public class RequestItemEndorsementException extends BadRequestException {

    public RequestItemEndorsementException(String message) {
        super(message, AppErrorCode.LPO_REQUEST);
    }
}
