package kkashin.dev.eventnotificator.kafka;

import kkashin.dev.kafka.EventChangedDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventChangedConsumer {

    public EventChangedConsumer() {}

    @KafkaListener(
            topics = "${event-notificator.topics.event-changed.name}",
            groupId = "${event-notificator.topics.event-changed.group-id}",
            containerFactory = "eventChangedDtoKafkaListenerContainerFactory"
    )
    public void consume(EventChangedDto message) {
        System.out.println(message);
    }
}
