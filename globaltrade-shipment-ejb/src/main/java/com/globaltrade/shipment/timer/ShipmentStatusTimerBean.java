package com.globaltrade.shipment.timer;

import com.globaltrade.core.constants.AlertTypes;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import com.globaltrade.interceptor.TimerLoggingInterceptor;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@Interceptors(TimerLoggingInterceptor.class)
public class ShipmentStatusTimerBean {

    private static final Logger logger = Logger.getLogger(ShipmentStatusTimerBean.class.getName());

    @Resource
    private TimerService timerService;

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @EJB
    private AlertPublisherLocal alertPublisher;

    public void scheduleStatusCheck(String trackingNumber, long delayMs) {

        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking number is required");
        }

        String info = "SHIPMENT_STATUS_TIMER:" + trackingNumber;
        TimerConfig config = new TimerConfig(info, true);

        timerService.createSingleActionTimer(delayMs, config);

        logger.info("Timer created: " + info + " (fires in " + delayMs + " ms)");
    }

    public void schedulePeriodicCheck(long intervalMs) {

        if (intervalMs <= 0) {
            throw new IllegalArgumentException("Invalid interval");
        }

        String info = "SHIPMENT_STATUS_TIMER:periodic";
        TimerConfig config = new TimerConfig(info, true);

        timerService.createIntervalTimer(intervalMs, intervalMs, config);

        logger.info("Periodic timer created: " + info + " (every " + intervalMs + " ms)");
    }


    @Timeout
    public void onTimeout(Timer timer) {

        String info = timer.getInfo() == null ? "" : timer.getInfo().toString();

        logger.info("@Timeout callback executing for: " + info);

        String prefix = "SHIPMENT_STATUS_TIMER:";

        if (info.startsWith(prefix)) {

            String suffix = info.substring(prefix.length());

            if ("periodic".equals(suffix)) {
                scanDelayedShipments();
            } else {
                checkShipmentAndAlert(suffix);
            }
        } else {
            scanDelayedShipments();
        }
    }

    public void cancelTimer(String timerInfo) {

        for (Timer timer : timerService.getTimers()) {
            if (timerInfo.equals(String.valueOf(timer.getInfo()))) {

                timer.cancel();
                logger.info("Timer cancelled: " + timerInfo);
                return;
            }
        }

        logger.warning("Timer not found for cancellation: " + timerInfo);
    }

    public void cancelAllTimers() {

        for (Timer timer : timerService.getTimers()) {
            timer.cancel();
        }

        logger.info("All shipment timers cancelled");
    }

    public List<String> listActiveTimers() {

        List<String> active = new ArrayList<>();

        for (Timer timer : timerService.getTimers()) {
            active.add(timer.getInfo().toString() + " | next: " + nextTimeout(timer) + " | remaining: "
                    + timer.getTimeRemaining() + "ms");
        }

        return active;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public String generateFleetStatusReport() {

        StringBuilder report = new StringBuilder();
        report.append("GlobalTrade Fleet & Timer Status Report \n");

        List<String> timers = listActiveTimers();

        report.append("Active timers: ")
                .append(timers.size())
                .append("\n");

        for (String timer : timers) {
            report.append("  - ").append(timer).append("\n");
        }

        long totalShipments = em.createNamedQuery("Shipment.findAll", Shipment.class).getResultList().size();

        long delayed = em.createNamedQuery("Shipment.findDelayed", Shipment.class)
                .setParameter("now", LocalDate.now())
                .getResultList().size();

        report.append("Total shipments: ").append(totalShipments).append("\n");
        report.append("Delayed shipments: ").append(delayed).append("\n");
        report.append("Generated at: ").append(LocalDate.now());

        return report.toString();
    }


    private void scanDelayedShipments() {
        List<Shipment> delayed = em.createNamedQuery("Shipment.findDelayed", Shipment.class)
                .setParameter("now", LocalDate.now())
                .getResultList();

        for (Shipment shipment : delayed) {
            alertPublisher.sendAlert(AlertTypes.SHIPMENT_DELAY,
                    "Shipment " + shipment.getTrackingNumber() + " [" + shipment.getShipmentType()
                            + "] is DELAYED at " + shipment.getCurrentLocation(), shipment.getId());
        }

        logger.info("Delayed shipment scan complete: " + delayed.size() + " alert(s) published");
    }

    private void checkShipmentAndAlert(String trackingNumber) {

        List<Shipment> results = em.createNamedQuery("Shipment.findByTrackingNumber", Shipment.class)
                .setParameter("trackingNumber", trackingNumber)
                .getResultList();

        if (results.isEmpty()) {
            logger.warning("Status check timer fired for unknown shipment: " + trackingNumber);
            return;
        }

        Shipment shipment = results.get(0);

        boolean overdue = shipment.getEstimatedDelivery() != null && shipment.getEstimatedDelivery().isBefore(LocalDate.now())
                && shipment.getStatus() != ShipmentStatus.DELIVERED && shipment.getStatus() != ShipmentStatus.CANCELLED;

        if (shipment.getStatus() == ShipmentStatus.DELAYED || overdue) {
            alertPublisher.sendAlert(AlertTypes.SHIPMENT_DELAY,
                    "Shipment " + trackingNumber + " requires attention: status: "
                            + shipment.getStatus() + ", location: "
                            + shipment.getCurrentLocation(), shipment.getId());
        } else {
            logger.fine("Status check OK for shipment " + trackingNumber);
        }
    }

    private String nextTimeout(Timer timer) {
        try {
            return String.valueOf(timer.getNextTimeout());
        } catch (Exception e) {
            return "none (single-action)";
        }
    }
}
