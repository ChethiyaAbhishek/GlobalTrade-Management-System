package com.globaltrade.core.exception;

import jakarta.ejb.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionContractTest {

    private ApplicationException annotationOf(Class<?> type) {
        return type.getAnnotation(ApplicationException.class);
    }

    @Test
    @DisplayName("ShipmentNotFoundException: application exception, rollback=false (recoverable read failure)")
    void shipmentNotFoundContract() {
        ApplicationException a = annotationOf(ShipmentNotFoundException.class);
        assertNotNull(a, "Must be an application exception");
        assertFalse(a.rollback(), "Lookup failures must not roll back");
        assertFalse(RuntimeException.class.isAssignableFrom(ShipmentNotFoundException.class));
    }

    @Test
    @DisplayName("Destructive business failures force rollback=true")
    void destructiveFailuresForceRollback() {
        assertTrue(annotationOf(InsufficientInventoryException.class).rollback());
        assertTrue(annotationOf(VendorValidationException.class).rollback());
        assertTrue(annotationOf(CustomsDeadlineException.class).rollback());
        assertTrue(annotationOf(OrderProcessingException.class).rollback());
    }

    @Test
    @DisplayName("Application exceptions are checked exceptions")
    void applicationExceptionsAreChecked() {
        for (Class<?> type : new Class<?>[]{
                InsufficientInventoryException.class, VendorValidationException.class,
                CustomsDeadlineException.class, OrderProcessingException.class}) {
            assertTrue(Exception.class.isAssignableFrom(type));
            assertFalse(RuntimeException.class.isAssignableFrom(type),
                    type.getSimpleName() + " must be checked");
        }
    }

    @Test
    @DisplayName("CarrierSystemException is a SYSTEM exception: RuntimeException, no annotation")
    void carrierIsSystemException() {
        assertTrue(RuntimeException.class.isAssignableFrom(CarrierSystemException.class));
        assertNull(annotationOf(CarrierSystemException.class),
                "Must NOT be marked @ApplicationException -> container wraps in EJBException");
    }

}
