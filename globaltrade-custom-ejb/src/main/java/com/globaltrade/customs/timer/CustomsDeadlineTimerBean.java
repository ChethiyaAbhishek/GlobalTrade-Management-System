package com.globaltrade.customs.timer;

import com.globaltrade.core.constants.AlertTypes;
import com.globaltrade.core.entity.CustomsDocument;
import com.globaltrade.core.messaging.AlertPublisherLocal;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class CustomsDeadlineTimerBean {

    private static final Logger logger = Logger.getLogger(CustomsDeadlineTimerBean.class.getName());

    private static final long ALERT_LEAD_TIME_MS = 24L * 60 * 60 * 1000;

    private static final int SCAN_WINDOW_DAYS = 3;

    @Resource
    private TimerService timerService;

    @PersistenceContext(unitName = "GlobalTrade-PU")
    private EntityManager em;

    @EJB
    private AlertPublisherLocal alertPublisher;

    public void scheduleDeadlineAlert(Long documentId, LocalDate deadline) {

        if (documentId == null || deadline == null) {
            throw new IllegalArgumentException("documentId and deadline are required");
        }

        Instant targetInstant = deadline.atStartOfDay(ZoneId.systemDefault()).toInstant()
                .minusMillis(ALERT_LEAD_TIME_MS);

        long delay = Duration.between(Instant.now(), targetInstant).toMillis();

        if (delay < 0) {
            delay = 0;
        }

        String info = "CUSTOMS_DEADLINE_TIMER:" + documentId;

        TimerConfig config = new TimerConfig(info, true);
        timerService.createSingleActionTimer(delay, config);

        logger.info("Deadline alert timer created: " + info + " (fires in " + delay + " ms)");
    }

    public void cancelDeadlineAlert(Long documentId) {

        String target = "CUSTOMS_DEADLINE_TIMER:" + documentId;

        for (Timer timer : timerService.getTimers()) {
            if (target.equals(String.valueOf(timer.getInfo()))) {
                timer.cancel();
                logger.info("Deadline timer cancelled: " + target);
                return;
            }
        }
    }

    @Timeout
    public void DeadlineApproaching(Timer timer) {

        String info = timer.getInfo() == null ? "" : timer.getInfo().toString();
        logger.info("@Timeout: customs deadline approaching: " + info);

        String split = info.substring(info.indexOf(':') + 1);
        Long documentId;

        try {
            documentId = Long.parseLong(split);
        } catch (NumberFormatException e) {
            logger.warning("Unparseable timer info: " + info);
            return;
        }

        CustomsDocument document = em.find(CustomsDocument.class, documentId);
        if (document == null) {
            logger.warning("Deadline timer fired for missing document " + documentId);
            return;
        }

        alertPublisher.sendAlert(AlertTypes.CUSTOMS_DEADLINE,
                "Customs document " + documentId + " (" + document.getType()
                        + ") deadline within 24 hours: " + document.getDeadline(), documentId);
    }


    @Schedule(hour = "7", persistent = true, info = "CUSTOMS_DEADLINE_TIMER")
    public void dailyDeadlineScan() {

        logger.info("TIMER: daily customs deadline scan started");

        List<CustomsDocument> approaching = em
                .createNamedQuery("CustomsDocument.findApproachingDeadlines", CustomsDocument.class)
                .setParameter("deadline", LocalDate.now().plusDays(SCAN_WINDOW_DAYS))
                .getResultList();

        for (CustomsDocument document : approaching) {
            alertPublisher.sendAlert(AlertTypes.CUSTOMS_DEADLINE,
                    "Customs document " + document.getId() + " (" + document.getType()
                            + ") has an approaching deadline: " + document.getDeadline(), document.getId());
        }

        logger.info("TIMER: deadline scan complete: " + approaching.size() + " alert(s) published");
    }
}

