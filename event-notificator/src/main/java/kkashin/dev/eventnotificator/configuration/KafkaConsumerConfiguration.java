package kkashin.dev.eventnotificator.configuration;

import kkashin.dev.kafka.EventChangedDto;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
public class KafkaConsumerConfiguration {

    @Bean
    public ConsumerFactory<String, EventChangedDto> eventChangedDtoConsumerFactory(
            KafkaProperties properties
    ) {
        return createConsumerFactory(
                properties,
                EventChangedDto.class
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventChangedDto> eventChangedDtoKafkaListenerContainerFactory(
            ConsumerFactory<String, EventChangedDto> eventChangedDtoConsumerFactory
    ) {
        return createContainerFactory(eventChangedDtoConsumerFactory);
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(
            KafkaProperties properties,
            Class<T> targetType
    ) {
        var jsonDeserializer = new JacksonJsonDeserializer<T>(targetType, false);

        jsonDeserializer.trustedPackages(targetType.getPackageName());

        var errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                errorHandlingDeserializer
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createContainerFactory(
            ConsumerFactory<String, T> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();

        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.setMissingTopicsFatal(false);

        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.RECORD
        );

        return factory;
    }
}
