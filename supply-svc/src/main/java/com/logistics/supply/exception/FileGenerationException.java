package com.logistics.supply.exception;

public class FileGenerationException extends BadRequestException
{
    public FileGenerationException(String message) {
        super(message, AppErrorCode.FILE_GENERATION);
    }
}
