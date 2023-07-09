package com.logistics.supply.exception;

public class VerificationTokenExpiredException extends BadRequestException {

    public VerificationTokenExpiredException(String token) {
        super("Verification token: %s has expired".formatted(token), AppErrorCode.VERIFICATION_TOKEN);
    }
}
