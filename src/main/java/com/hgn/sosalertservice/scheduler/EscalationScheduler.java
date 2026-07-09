package com.hgn.sosalertservice.scheduler;

import com.hgn.sosalertservice.entity.Alert;
import com.hgn.sosalertservice.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EscalationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EscalationScheduler.class);

    private final AlertRepository alertRepository;

    public EscalationScheduler(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void escalateAlerts() {
        logger.debug("Running scheduled SOS alert escalation check...");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        List<Alert> unresolvedAlerts = alertRepository.findNewAlertsOlderThan(threshold);

        if (!unresolvedAlerts.isEmpty()) {
            logger.info("Found {} open alerts older than 10 minutes. Escalating...", unresolvedAlerts.size());
            for (Alert alert : unresolvedAlerts) {
                alert.setStatus("ESCALATED");
                alert.setUrgent(true);
                alertRepository.save(alert);
                logger.info("Escalated Alert ID {} for device {}", alert.getId(), alert.getDevice().getDeviceId());
            }
        }
    }
}
