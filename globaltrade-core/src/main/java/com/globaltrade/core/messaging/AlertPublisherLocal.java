package com.globaltrade.core.messaging;

import jakarta.ejb.Local;

@Local
public interface AlertPublisherLocal {
    void sendAlert(String alertType, String msg, Long entityId);
}
