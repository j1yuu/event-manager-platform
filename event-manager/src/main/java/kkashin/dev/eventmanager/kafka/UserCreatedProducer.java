package kkashin.dev.eventmanager.kafka;

import kkashin.dev.eventmanager.config.properties.KafkaTopicsProperties;
import kkashin.dev.kafka.UserCreatedDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserCreatedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public UserCreatedProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = properties.topics().userCreated().name();
    }

    public void send(UserCreatedDto message) {
        var uuid = UUID.randomUUID().toString();

        kafkaTemplate.send(
                topic,
                uuid,
                message
        );
    }
}

