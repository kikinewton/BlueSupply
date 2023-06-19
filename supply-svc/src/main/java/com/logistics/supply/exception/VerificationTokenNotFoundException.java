package com.logistics.supply.exception;

public class VerificationTokenNotFoundException extends NotFoundException {

    public VerificationTokenNotFoundException(String token) {
        super("Verification token: {} not found".formatted(token));
    }
}
