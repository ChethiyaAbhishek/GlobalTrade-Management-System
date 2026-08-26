package com.globaltrade.messaging.mdb;

import com.globaltrade.core.entity.Alert;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/GlobalTradeAlertQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @ActivationConfigProperty(propertyName = "maxPoolSize", propertyValue = "8")
})
public class AlertMDB implements MessageListener {

    private static final Logger logger = Logger.getLogger(AlertMDB.class.getName());

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @Override
    public void onMessage(Message message) {
        try {

            if (!(message instanceof MapMessage)) {
                logger.warning("AlertMDB received non-map message - ignored: " + message);
                return;
            }

            MapMessage mapMessage = (MapMessage) message;
            String alertType = mapMessage.getString("alertType");
            String text = mapMessage.getString("message");
            long entityId = mapMessage.getLong("entityId");

            Alert alert = new Alert(alertType, text, entityId == 0 ? null : entityId, "AlertMDB");
            em.persist(alert);

            logger.info("ALERT PROCESSED [" + alertType + "]: " + text);

        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Error processing alert message", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in AlertMDB", e);
        }
    }
}
