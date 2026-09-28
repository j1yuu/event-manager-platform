package kkashin.dev.eventnotificator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@ConfigurationPropertiesScan
@EnableCaching
public class EventNotificatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventNotificatorApplication.class, args);
    }

}
