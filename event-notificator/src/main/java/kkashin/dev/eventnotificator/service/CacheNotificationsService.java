package kkashin.dev.eventnotificator.service;

import kkashin.dev.eventnotificator.model.domain.NotificationsChangedBatchEvent;
import kkashin.dev.eventnotificator.model.domain.NotificationsChangedEvent;
import kkashin.dev.eventnotificator.repository.NotificationRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
public class CacheNotificationsService {

    private final NotificationRepository notificationRepository;
    private final StringRedisTemplate redis;

    private final String NOTIFICATIONS_COUNT_PREFIX = "notif:unread:";

    public CacheNotificationsService(
            NotificationRepository notificationRepository,
            StringRedisTemplate redis
    ) {
        this.notificationRepository = notificationRepository;
        this.redis = redis;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private void handleSingle(NotificationsChangedEvent event) {
        var userId = event.userId();
        var unreadCount = notificationRepository.getUnreadCountForUserById(userId);

        set(userId, unreadCount);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private void handleBatch(NotificationsChangedBatchEvent event) {
        var userUnreadPairs = notificationRepository.getUnreadCountByUserId(event.userIds());

        userUnreadPairs
                .forEach((row) -> set(row.getUserId(), row.getUnreadCount()));
    }

    private void set(Long userId, Long count) {
        redis
                .opsForValue()
                .set(
                        key(userId),
                        String.valueOf(count),
                        Duration.of(10, ChronoUnit.MINUTES)
                );
    }

    public Long get(Long userId) {
        var val = redis
                .opsForValue()
                .get(key(userId));

        return val == null
                ? 0
                : Long.parseLong(val);
    }

    private String key(Long userId) {
        return NOTIFICATIONS_COUNT_PREFIX + userId.toString();
    }
}
