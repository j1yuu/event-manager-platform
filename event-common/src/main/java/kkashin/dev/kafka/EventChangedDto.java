package kkashin.dev.kafka;

import java.time.Instant;
import java.util.List;

public record EventChangedDto(
    String messageId,
    EventType eventType,
    Long eventId,
    Instant occurredAt,
    String eventName,
    String message,
    Long ownerId,
    Long changedById,
    List<Long> subscribers,
    List<EventChangedFieldDto> changes
) {
}
