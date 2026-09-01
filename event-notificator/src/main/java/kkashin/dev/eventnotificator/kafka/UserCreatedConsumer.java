package kkashin.dev.eventnotificator.kafka;

import kkashin.dev.kafka.UserCreatedDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedConsumer {

    public UserCreatedConsumer() {}

    @KafkaListener(
            topics = "${event-notificator.topics.user-created.name}",
            groupId = "${event-notificator.topics.user-created.group-id}",
            containerFactory = "userCreatedDtoKafkaListenerContainerFactory"
    )
    public void consume(UserCreatedDto message) {
        System.out.println(message);
    }
}
