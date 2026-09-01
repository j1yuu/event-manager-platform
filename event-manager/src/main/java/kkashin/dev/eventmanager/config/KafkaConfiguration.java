package kkashin.dev.eventmanager.config;

import kkashin.dev.eventmanager.config.properties.KafkaTopicsProperties;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaConfiguration {

    @Bean
    public KafkaAdmin.NewTopics eventManagerTopics(
            KafkaTopicsProperties kafkaTopicsProperties
    ) {
        var eventUpdated = kafkaTopicsProperties.topics().eventUpdated();
        var userCreated = kafkaTopicsProperties.topics().userCreated();

        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(eventUpdated.name())
                        .partitions(eventUpdated.partitions())
                        .replicas(eventUpdated.replicas())
                        .config(
                                TopicConfig.CLEANUP_POLICY_CONFIG,
                                TopicConfig.CLEANUP_POLICY_DELETE
                        )
                        .config(
                                TopicConfig.RETENTION_MS_CONFIG,
                                Long.toString(eventUpdated.retention().toMillis())
                        )
                        .config(
                                TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                                Integer.toString(eventUpdated.minInSyncReplicas())
                        )
                        .build(),
                TopicBuilder.name(userCreated.name())
                        .partitions(userCreated.partitions())
                        .replicas(userCreated.replicas())
                        .config(
                                TopicConfig.CLEANUP_POLICY_CONFIG,
                                TopicConfig.CLEANUP_POLICY_DELETE
                        )
                        .config(
                                TopicConfig.RETENTION_MS_CONFIG,
                                Long.toString(userCreated.retention().toMillis())
                        )
                        .config(
                                TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
                                Integer.toString(userCreated.minInSyncReplicas())
                        )
                        .build()
        );
    }
}
