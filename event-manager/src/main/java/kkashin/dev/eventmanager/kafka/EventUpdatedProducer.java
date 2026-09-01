package kkashin.dev.eventmanager.kafka;

import kkashin.dev.eventmanager.config.properties.KafkaTopicsProperties;
import kkashin.dev.kafka.EventChangedDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EventUpdatedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public EventUpdatedProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicsProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = properties.topics().eventUpdated().name();
    }

    public void send(EventChangedDto message) {
        var uuid = UUID.randomUUID().toString();

        kafkaTemplate.send(
                topic,
                uuid,
                message
        );
    }
}
