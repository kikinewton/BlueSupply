package com.logistics.supply.exception;

public class RequestItemUpdateDetailsException extends BadRequestException {

    public RequestItemUpdateDetailsException(int requestItemId) {
        super("Failed to update details of request item with id: %s".formatted(requestItemId), AppErrorCode.LPO_REQUEST);
    }
}
