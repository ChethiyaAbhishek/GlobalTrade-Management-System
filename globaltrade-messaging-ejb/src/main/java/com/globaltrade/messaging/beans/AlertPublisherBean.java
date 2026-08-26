package com.globaltrade.messaging.beans;

import com.globaltrade.core.messaging.AlertPublisherLocal;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class AlertPublisherBean implements AlertPublisherLocal {

    private static final Logger logger = Logger.getLogger(AlertPublisherBean.class.getName());

    @Resource(lookup = "jms/GlobalTradeAlertQueue")
    private Queue alertQueue;

    @Inject
    @JMSConnectionFactory("jms/GlobalTradeConnectionFactory")
    private JMSContext jmsContext;

    @Override
    public void sendAlert(String alertType, String message, Long entityId) {
        try {
            MapMessage alertMessage = jmsContext.createMapMessage();
            alertMessage.setString("alertType", alertType);
            alertMessage.setString("message", message);

            if (entityId != null) {
                alertMessage.setLong("entityId", entityId);
            }

            alertMessage.setLong("publishedAt", System.currentTimeMillis());

            JMSProducer producer = jmsContext.createProducer();
            producer.send(alertQueue, alertMessage);

            logger.fine("Alert published to queue: [" + alertType + "] " + message);

        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Failed to publish alert [" + alertType + "]: " + message, e);
        }
    }
}
