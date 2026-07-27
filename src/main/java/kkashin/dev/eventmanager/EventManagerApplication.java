package kkashin.dev.eventmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableWebSecurity
@EnableMethodSecurity
public class EventManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagerApplication.class, args);
    }

}
