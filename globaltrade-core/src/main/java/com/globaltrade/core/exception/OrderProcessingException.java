package com.globaltrade.core.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class OrderProcessingException extends Exception {
    public OrderProcessingException(String message) {
        super(message);
    }
}
