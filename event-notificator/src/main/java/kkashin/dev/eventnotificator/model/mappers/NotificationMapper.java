package kkashin.dev.eventnotificator.model.mappers;

import kkashin.dev.eventnotificator.model.dto.NotificationDto;
import kkashin.dev.eventnotificator.model.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getPayload().getEventType(),
                notification.getPayload().getEventId(),
                notification.getCreatedAt(),
                notification.getIsRead(),
                "",
                new NotificationDto.Payload(
                        notification.getPayload().getEventType(),
                        notification.getPayload().getCreatedAt(),
                        notification.getPayload().getChangedBy(),
                        notification.getPayload().getOwnerId(),
                        notification.getPayload().getEventName(),
                        notification.getPayload().getPayload()
                )
        );
    }
}
