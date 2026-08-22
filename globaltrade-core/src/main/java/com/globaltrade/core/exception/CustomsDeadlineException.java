package com.globaltrade.core.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CustomsDeadlineException extends Exception {
    public CustomsDeadlineException(String message) {
        super(message);
    }
}
