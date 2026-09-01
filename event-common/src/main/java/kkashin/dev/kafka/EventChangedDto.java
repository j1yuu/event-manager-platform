package kkashin.dev.kafka;

import java.time.Instant;
import java.util.List;

public record EventChangedDto(
    String messageId,
    String eventType,
    Long eventId,
    Instant occurredAt,
    Long ownerId,
    Long changedById,
    List<Long> subscribers,
    List<EventChangedFieldDto> changedFields
) {
}
