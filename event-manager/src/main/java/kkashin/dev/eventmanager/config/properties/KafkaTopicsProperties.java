package kkashin.dev.eventmanager.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix="event-manager.kafka")
public record KafkaTopicsProperties(
    Topics topics
) {
    public record Topics(
            Topic eventUpdated,
            Topic userCreated
    ) {}

    public record Topic(
            String name,
            int partitions,
            int replicas,
            Duration retention,
            int minInSyncReplicas
    ) {}
}
