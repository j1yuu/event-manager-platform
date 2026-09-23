package kkashin.dev.eventnotificator.model.dto;

import kkashin.dev.kafka.EventChangedFieldDto;
import kkashin.dev.kafka.EventType;

import java.time.Instant;
import java.util.List;

public record NotificationDto (
        Long notificationId,
        EventType type,
        Long eventId,
        Instant createdAt,
        String message,
        Boolean isRead,
        Payload payload
) {
    public record Payload(
            String messageId,
            EventType eventType,
            Instant occurredAt,
            Long changedById,
            Long ownerId,
            String eventName,
            List<EventChangedFieldDto> changes
    ) {}
}
