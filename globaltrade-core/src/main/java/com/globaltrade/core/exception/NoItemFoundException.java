package com.globaltrade.core.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NoItemFoundException extends Exception {
    public NoItemFoundException(Long itemId) {
        super("No item found by inventory item id: "+ itemId);
    }
}
