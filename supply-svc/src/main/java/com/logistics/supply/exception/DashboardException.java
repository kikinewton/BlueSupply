package com.logistics.supply.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardException extends RuntimeException{
    private String message;

    public DashboardException(String message) {
        this.message = message;
    }
}
