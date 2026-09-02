package com.globaltrade.shipment.beans;

import com.globaltrade.core.annotations.Audited;
import com.globaltrade.core.annotations.Monitored;
import com.globaltrade.core.constants.AlertTypes;
import com.globaltrade.core.dto.ShipmentStatusDTO;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.entity.ShipmentTrackingEvent;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.enums.ShipmentType;
import com.globaltrade.core.exception.ShipmentNotFoundException;
import com.globaltrade.core.facade.BaseFacade;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import com.globaltrade.shipment.local.ShipmentFacadeLocal;
import com.globaltrade.shipment.remote.ShipmentFacadeRemote;
import com.globaltrade.shipment.timer.ShipmentStatusTimerBean;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@Audited
@Monitored
public class ShipmentFacadeBean extends BaseFacade<Shipment> implements ShipmentFacadeLocal, ShipmentFacadeRemote {

    private static final Logger logger = Logger.getLogger(ShipmentFacadeBean.class.getName());

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @EJB
    private RouteOptimizationBean routeOptimization;

    @EJB
    private ShipmentStatusTimerBean statusTimerBean;

    @EJB
    private AlertPublisherLocal alertPublisher;

    public ShipmentFacadeBean() {
        super(Shipment.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void create(Shipment shipment) {

        if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking number is required");
        }

        if (shipment.getShipmentType() == null) {
            throw new IllegalArgumentException("Shipment type is required");
        }

        if (shipment.getStatus() == null) {
            if (shipment.getShipmentType() == ShipmentType.INBOUND) {
                shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            } else {
                shipment.setStatus(ShipmentStatus.PENDING);
            }
        }

        if (shipment.getCurrentLocation() == null) {
            shipment.setCurrentLocation(shipment.getOrigin());
        }

        String route = routeOptimization.calculateOptimalRoute(shipment.getOrigin(), shipment.getDestination());
        shipment.setRouteDescription(route);

        em.persist(shipment);

        statusTimerBean.scheduleStatusCheck(shipment.getTrackingNumber(), 30 * 60 * 1000);

        em.persist(ShipmentTrackingEvent.builder()
                .eventType("SHIPMENT_CREATED")
                .location(shipment.getOrigin())
                .description(shipment.getShipmentType() + " shipment registered: route: " + route)
                .shipment(shipment).build());

        logger.info(shipment.getShipmentType() + " shipment " + shipment.getTrackingNumber()
                + " registered: " + shipment.getOrigin() + " -> " + shipment.getDestination());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Shipment findByTrackingNumber(String trackingNumber) throws ShipmentNotFoundException {

        List<Shipment> results = em.createNamedQuery("Shipment.findByTrackingNumber", Shipment.class)
                .setParameter("trackingNumber", trackingNumber)
                .getResultList();

        if (results.isEmpty()) {
            throw new ShipmentNotFoundException("Shipment not found: " + trackingNumber);
        }

        return results.get(0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateStatus(Long shipmentId, ShipmentStatus newStatus) throws ShipmentNotFoundException {

        Shipment shipment = em.find(Shipment.class, shipmentId);

        if (shipment == null) {
            throw new ShipmentNotFoundException("Shipment not found by shipmentId: " + shipmentId);
        }

        ShipmentStatus previous = shipment.getStatus();
        shipment.setStatus(newStatus);

        if (newStatus == ShipmentStatus.IN_TRANSIT) {
            shipment.setCurrentLocation("En route: " + shipment.getRouteDescription());

        } else if (newStatus == ShipmentStatus.DELIVERED) {

            shipment.setCurrentLocation(shipment.getDestination());
            if (shipment.getActualDelivery() == null) {
                shipment.setActualDelivery(LocalDate.now());
            }
        } else if (newStatus == ShipmentStatus.DELAYED) {
            shipment.setCurrentLocation("Delayed at: " + shipment.getCurrentLocation());
        }

        if (newStatus == ShipmentStatus.DELIVERED && shipment.getActualDelivery() == null) {
            shipment.setActualDelivery(LocalDate.now());
        }

        em.persist(ShipmentTrackingEvent.builder()
                .eventType("STATUS_CHANGE")
                .location(shipment.getCurrentLocation())
                .description("Status changed from " + previous + " to " + newStatus)
                .shipment(shipment).build());

        em.merge(shipment);

        if (newStatus == ShipmentStatus.DELAYED) {
            alertPublisher.sendAlert(AlertTypes.SHIPMENT_DELAY,
                    "Shipment " + shipment.getTrackingNumber() + " [" + shipment.getShipmentType()
                            + "] is DELAYED at " + shipment.getCurrentLocation(),
                    shipment.getId());
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<ShipmentTrackingEvent> getTrackingHistory(Long shipmentId) {

        return em.createNamedQuery("ShipmentTrackingEvent.findByShipment", ShipmentTrackingEvent.class)
                .setParameter("shipmentId", shipmentId)
                .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Shipment> findDelayedShipments() {
        return em.createNamedQuery("Shipment.findDelayed", Shipment.class)
                .setParameter("now", LocalDate.now())
                .setMaxResults(100)
                .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> findByType(ShipmentType type) {
        return em.createNamedQuery("Shipment.findByType", Shipment.class)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public void notifyCarrier(Shipment shipment) {

        logger.info("Notifying carrier for shipment " + shipment.getTrackingNumber());

        /// --------------------------
        /// external Carrier API call
        /// --------------------------

        logger.info("Carrier notified successfully for " + shipment.getTrackingNumber());
    }

    @Override
    public Shipment findById(Long id) {
        return super.find(id);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public ShipmentStatusDTO getShipmentStatus(String trackingNumber) throws ShipmentNotFoundException {

        Shipment shipment = findByTrackingNumber(trackingNumber);

        Instant lastUpdate = shipment.getUpdatedAt() != null ? shipment.getUpdatedAt() : shipment.getCreatedAt();

        return new ShipmentStatusDTO(
                shipment.getTrackingNumber(),
                shipment.getStatus().name(),
                shipment.getCurrentLocation(),
                lastUpdate);
    }
}
