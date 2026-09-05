package kkashin.dev.eventmanager.kafka;

import org.springframework.kafka.support.SendResult;
import kkashin.dev.eventmanager.config.properties.KafkaTopicsProperties;
import kkashin.dev.kafka.EventChangedDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    public CompletableFuture<SendResult<String, Object>> send(EventChangedDto message) {
        return kafkaTemplate.send(
                topic,
                message.messageId(),
                message
        );
    }

    public CompletableFuture<Void> sendAll(List<EventChangedDto> messages) {
        var futures = messages.stream()
                .map(this::send)
                .toArray(CompletableFuture<?>[]::new);

        return CompletableFuture.allOf(futures);
    }
}
