package com.globaltrade.customs.timer;

import com.globaltrade.core.constants.AlertTypes;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import com.globaltrade.customs.beans.CustomsDocumentationBean;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.logging.Logger;

@Stateless
public class CustomsDocumentationTimerBean {

    private static final Logger logger = Logger.getLogger(CustomsDocumentationTimerBean.class.getName());

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @EJB
    private CustomsDocumentationBean customsDocumentationBean;

    @EJB
    private AlertPublisherLocal alertPublisher;

    @Schedule(hour = "*/8", persistent = true, info = "CUSTOMS_DOCUMENTATION_SERVICE")
    public void submitPendingDocumentation() {

        logger.info("TIMER: customs documentation service started");

        List<Shipment> shipmentList = em.createNamedQuery("Shipment.findInternationalOutboundWithoutDocuments", Shipment.class)
                .getResultList();

        for (Shipment shipment : shipmentList) {
            try {
                customsDocumentationBean.submitDocumentationPackage(shipment);
            } catch (Exception e) {
                alertPublisher.sendAlert(AlertTypes.CUSTOMS_DEADLINE,
                        "Customs documentation submission FAILED for shipment "
                                + shipment.getTrackingNumber() + ": " + e.getMessage(), shipment.getId());
            }
        }

        logger.info("TIMER: customs documentation service complete: " + shipmentList.size() + " submitted ");
    }
}
