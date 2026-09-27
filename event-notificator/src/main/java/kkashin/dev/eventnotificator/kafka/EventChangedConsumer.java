package kkashin.dev.eventnotificator.kafka;

import kkashin.dev.eventnotificator.service.NotificationService;
import kkashin.dev.kafka.EventChangedDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventChangedConsumer {

    private final NotificationService service;

    public EventChangedConsumer(
            NotificationService service
    ) {
        this.service = service;
    }

    @KafkaListener(
            topics = "${event-notificator.kafka.topics.event-changed.name}",
            groupId = "${event-notificator.kafka.topics.event-changed.group-id}",
            containerFactory = "eventChangedDtoKafkaListenerContainerFactory"
    )
    public void consume(EventChangedDto message) {
        service.consume(message);
    }
}
