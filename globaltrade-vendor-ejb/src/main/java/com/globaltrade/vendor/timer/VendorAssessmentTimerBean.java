package com.globaltrade.vendor.timer;

import com.globaltrade.core.constants.AlertTypes;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.entity.VendorPerformance;
import com.globaltrade.core.enums.VendorStatus;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import com.globaltrade.vendor.local.VendorFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class VendorAssessmentTimerBean {

    private static final Logger logger = Logger.getLogger(VendorAssessmentTimerBean.class.getName());

    private static final double LOW_SCORE_THRESHOLD = 60.0;

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @EJB
    private VendorFacadeLocal vendorFacade;

    @EJB
    private AlertPublisherLocal alertPublisher;

    @Schedule(dayOfWeek = "Mon", hour = "1", persistent = true, info = "VENDOR_ASSESSMENT_TIMER")
    public void weeklyVendorAssessment() {
        assessAllActiveVendors();
    }

    public void runAssessmentNow() {
        assessAllActiveVendors();
    }

    private void assessAllActiveVendors() {
        logger.info("TIMER: vendor performance assessment started");

        List<Vendor> activeVendors = em.createNamedQuery("Vendor.findByStatus", Vendor.class)
                .setParameter("status", VendorStatus.ACTIVE)
                .getResultList();

        int assessed = 0;
        int skipped = 0;

        for (Vendor vendor : activeVendors) {
            ShipmentMetrics metrics = calculateMetricsFromInboundShipments(vendor.getId());

            if (metrics.totalShipments == 0) {
                skipped++;
                logger.fine("Skipped vendor " + vendor.getName() + ": no inbound shipment history");
                continue;
            }

            VendorPerformance performance = vendorFacade.recordEvaluation(
                    vendor.getId(),
                    metrics.onTimeDeliveryRate,
                    metrics.defectRate,
                    "AUTO-" + LocalDate.now());

            assessed++;
            logger.info("Vendor " + vendor.getName() + " assessed from "
                    + metrics.totalShipments + " inbound shipment(s): onTime: "
                    + metrics.onTimeDeliveryRate + "%, defects: "
                    + metrics.defectRate + "%, score: " + performance.getOverallScore());

            if (performance.getOverallScore() < LOW_SCORE_THRESHOLD) {
                alertPublisher.sendAlert(AlertTypes.VENDOR_PERFORMANCE,
                        "Vendor " + vendor.getName() + " scored below threshold: "
                                + performance.getOverallScore()
                                + " (threshold " + LOW_SCORE_THRESHOLD + ")",
                        vendor.getId());
            }
        }

        logger.info("TIMER: assessment complete: " + assessed + " assessed, "
                + skipped + " skipped (no inbound history)");
    }

    private ShipmentMetrics calculateMetricsFromInboundShipments(Long vendorId) {
        List<Shipment> inboundShipments = em.createNamedQuery("Shipment.findInboundByVendor", Shipment.class)
                .setParameter("vendorId", vendorId)
                .getResultList();

        int total = inboundShipments.size();
        int delivered = 0;
        int deliveredOnTime = 0;
        int problemShipments = 0;

        for (Shipment shipment : inboundShipments) {
            if (shipment.getStatus() == null) {
                continue;
            }
            switch (shipment.getStatus()) {
                case DELIVERED:
                    delivered++;
                    if (deliveredOnTime(shipment)) {
                        deliveredOnTime++;
                    }
                    break;
                case DELAYED:
                case CANCELLED:
                    problemShipments++;
                    break;
                default:
                    break;
            }
        }

        double onTimeRate = delivered == 0 ? 0.0 : (deliveredOnTime * 100.0) / delivered;

        double defectRate = total == 0 ? 0.0 : (problemShipments * 100.0) / total;

        return new ShipmentMetrics(total, onTimeRate, defectRate);
    }

    private boolean deliveredOnTime(Shipment shipment) {
        return shipment.getEstimatedDelivery() != null
                && shipment.getActualDelivery() != null
                && !shipment.getActualDelivery().isAfter(shipment.getEstimatedDelivery());
    }

    private record ShipmentMetrics(
            int totalShipments,
            double onTimeDeliveryRate,
            double defectRate
    ) {
    }
}
