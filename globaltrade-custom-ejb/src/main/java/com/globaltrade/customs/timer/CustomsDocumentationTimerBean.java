package com.globaltrade.customs.timer;

import com.globaltrade.core.constants.AlertTypes;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import com.globaltrade.customs.beans.CustomsDocumentationBean;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
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

    @Resource
    private TimerService timerService;

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

    public List<String> listMyTimers() {
        List<String> list = new ArrayList<>();
        for (Timer t : timerService.getTimers()) {
            list.add(getClass().getSimpleName() + " | " + t.getInfo()
                    + " | next: " + safeNext(t));
        }
        return list;
    }

    private String safeNext(Timer t) {
        try { return String.valueOf(t.getNextTimeout()); }
        catch (Exception e) { return "single-action"; }
    }
}
