package kkashin.dev.eventmanager.security.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event-manager.admin")
public record AdminProperties(
        String login,
        String password
) {
}
