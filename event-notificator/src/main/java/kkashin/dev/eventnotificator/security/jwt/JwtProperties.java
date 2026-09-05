package kkashin.dev.eventnotificator.security.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import java.time.Duration;

@ConfigurationProperties(prefix = "event-notificator.jwt")
public record JwtProperties(
        String issuer,
        Duration accessTtl,
        String secretBase64
        ) {

        public SecretKey secretKey() {
                byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
                return Keys.hmacShaKeyFor(keyBytes);
        }
}
