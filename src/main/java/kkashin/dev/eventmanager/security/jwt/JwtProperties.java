package kkashin.dev.eventmanager.security.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;

@ConfigurationProperties(prefix = "event-manager.jwt")
public record JwtProperties(
        String issuer,
        Integer accessTtl,
        String secretBase64
        ) {

        public SecretKey secretKey() {
                byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
                return Keys.hmacShaKeyFor(keyBytes);
        }
}
