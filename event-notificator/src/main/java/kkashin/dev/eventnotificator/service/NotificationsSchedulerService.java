package kkashin.dev.eventnotificator.service;

import kkashin.dev.eventnotificator.repository.NotificationPayloadRepository;
import kkashin.dev.eventnotificator.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class NotificationsSchedulerService {
    private final NotificationRepository notificationRepository;
    private final NotificationPayloadRepository notificationPayloadRepository;

    private final Clock clock;
    private final Long secondsToSubstract = 604800L;

    public NotificationsSchedulerService(
            NotificationRepository notificationRepository,
            NotificationPayloadRepository notificationPayloadRepository,
            Clock clock
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationPayloadRepository = notificationPayloadRepository;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${event-notificator.scheduler.status-delay-ms:60000}")
    @Transactional
    public void deleteDeprecated() {
        var timestamp = clock.instant().minusSeconds(secondsToSubstract);

        notificationRepository.removeOldReads(timestamp);
        notificationPayloadRepository.removeWithoutNotifications();
    }
}
