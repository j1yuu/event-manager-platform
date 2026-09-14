package kkashin.dev.eventnotificator.service;

import kkashin.dev.eventnotificator.model.dto.NotificationDto;
import kkashin.dev.eventnotificator.model.mappers.EventChangedMapper;
import kkashin.dev.eventnotificator.model.mappers.NotificationMapper;
import kkashin.dev.eventnotificator.repository.NotificationPayloadRepository;
import kkashin.dev.eventnotificator.repository.NotificationRepository;
import kkashin.dev.kafka.EventChangedDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPayloadRepository payloadRepository;

    private final UserService userService;
    private final EventChangedMapper eventChangedMapper;
    private final NotificationMapper notificationMapper;

    private final Clock clock;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationPayloadRepository payloadRepository,
            UserService userService,
            EventChangedMapper eventChangedMapper,
            NotificationMapper notificationMapper,
            Clock clock
    ) {
        this.notificationRepository = notificationRepository;
        this.payloadRepository = payloadRepository;
        this.userService = userService;
        this.eventChangedMapper = eventChangedMapper;
        this.notificationMapper = notificationMapper;
        this.clock = clock;
    }

    @Transactional
    public void consume(EventChangedDto message) {
        var userIds = message.subscribers();

        var payload = eventChangedMapper.toPayload(message);
        var notifications = userIds.stream()
                .map(id -> eventChangedMapper.toNotification(id, payload))
                .toList();

        payloadRepository.save(payload);
        notificationRepository.saveAll(notifications);
    }

    public List<NotificationDto> getUnreads() {
        var currentUser = userService.currentUser();

        var notificationEntities = notificationRepository.getNotificationsByUserId(currentUser.getId());

        return notificationEntities.stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Transactional
    public void readNotifications(List<Long> notificationIds) {
        if (notificationIds.isEmpty()) return;

        var currentUser = userService.currentUser();
        var now = clock.instant();

        notificationRepository.markRead(notificationIds, now, currentUser.getId());
    }
}
