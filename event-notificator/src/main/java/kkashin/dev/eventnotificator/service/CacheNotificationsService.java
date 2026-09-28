package kkashin.dev.eventnotificator.service;

import kkashin.dev.eventnotificator.model.domain.NotificationsChangedBatchEvent;
import kkashin.dev.eventnotificator.model.domain.NotificationsChangedEvent;
import kkashin.dev.eventnotificator.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class CacheNotificationsService {

    private static final String NOTIFICATIONS_COUNT_PREFIX = "notif:unread:";
    private static final Logger log = LoggerFactory.getLogger(CacheNotificationsService.class);

    private final NotificationRepository notificationRepository;
    private final StringRedisTemplate redis;

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
    void handleBatch(NotificationsChangedBatchEvent event) {
        event.incrementsByUserId().forEach(this::increment);
    }

    private void set(Long userId, Long count) {
        try {
            redis.opsForValue().set(key(userId), String.valueOf(count));
        } catch (RuntimeException e) {
            log.warn("Unable to sync unread counter for userId={}", userId, e);
        }
    }

    private void increment(Long userId, Long delta) {
        try {
            redis.opsForValue().increment(key(userId), delta);
        } catch (RuntimeException e) {
            log.warn("Unable to increment unread counter for userId={}, delta={}", userId, delta, e);
        }
    }

    public Long get(Long userId) {
        try {
            var val = redis.opsForValue()
                    .get(key(userId));

            if (val != null) {
                return Long.parseLong(val);
            }
        } catch (Exception e) {
            log.warn("Unable to get unread counter for userId={}", userId, e);
        }

        var count = notificationRepository.getUnreadCountForUserById(userId);
        set(userId, count);

        return count;
    }

    private String key(Long userId) {
        return NOTIFICATIONS_COUNT_PREFIX + userId.toString();
    }
}
