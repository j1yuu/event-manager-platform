package kkashin.dev.eventnotificator.service;

import kkashin.dev.eventnotificator.model.dto.MarkReadRequestDto;
import kkashin.dev.eventnotificator.model.dto.NotificationDto;
import kkashin.dev.eventnotificator.model.mappers.NotificationMapper;
import kkashin.dev.eventnotificator.repository.NotificationPayloadRepository;
import kkashin.dev.eventnotificator.repository.NotificationRepository;
import kkashin.dev.kafka.EventChangedDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPayloadRepository payloadRepository;

    private final UserService userService;
    private final NotificationMapper notificationMapper;

    private final Clock clock;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationPayloadRepository payloadRepository,
            UserService userService,
            NotificationMapper notificationMapper,
            Clock clock
    ) {
        this.notificationRepository = notificationRepository;
        this.payloadRepository = payloadRepository;
        this.userService = userService;
        this.notificationMapper = notificationMapper;
        this.clock = clock;
    }

    @Transactional
    public void consume(EventChangedDto message) {
        var userIds = message.subscribers();

        var changesMapped = objectMapper.writeValueAsString(message.changes());

        var payloadId = payloadRepository.insertIfAbsentReturningId(
                message.messageId(),
                message.eventType().toString(),
                message.eventName(),
                message.message(),
                message.eventId(),
                message.changedById(),
                message.ownerId(),
                changesMapped,
                message.occurredAt()
        );

        notificationRepository.insertIfAbsentBatch(userIds.toArray(Long[]::new), payloadId);
    }

    public List<NotificationDto> getUnread() {
        var currentUser = userService.currentUser();

        var notificationEntities = notificationRepository.getNotificationsByUserId(currentUser.getId());

        return notificationEntities.stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Transactional
    public void readNotifications(MarkReadRequestDto dto) {
        var currentUser = userService.currentUser();
        var now = clock.instant();

        notificationRepository.markRead(dto.notificationIds(), now, currentUser.getId());
    }
}
