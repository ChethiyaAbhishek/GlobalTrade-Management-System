package com.globaltrade.core.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class ShipmentNotFoundException extends Exception {
    public ShipmentNotFoundException(String message) {
        super(message);
    }
}
