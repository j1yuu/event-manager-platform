package kkashin.dev.eventnotificator.model.mappers;

import kkashin.dev.eventnotificator.model.entity.Notification;
import kkashin.dev.eventnotificator.model.entity.NotificationPayload;
import kkashin.dev.kafka.EventChangedDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventChangedMapper {

    public NotificationPayload toPayload(EventChangedDto dto) {
        var payload = new NotificationPayload();

        payload.setMessageId(dto.messageId());
        payload.setEventType(dto.eventType());
        payload.setEventId(dto.eventId());
        payload.setEventName(dto.eventName());
        payload.setChangedBy(dto.changedById());
        payload.setOwnerId(dto.ownerId());
        payload.setPayload(dto.changedFields());

        return payload;
    }

    public Notification toNotification(Long userId, NotificationPayload payload) {
        var notification = new Notification();

        notification.setUserId(userId);
        notification.setPayload(payload);

        return notification;
    }
}
