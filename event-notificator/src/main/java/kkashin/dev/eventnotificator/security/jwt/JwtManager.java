package kkashin.dev.eventnotificator.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtManager {

    private final JwtProperties jwtProperties;

    public JwtManager(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public Claims getPayload(String jwt) {
        return Jwts.parser()
                .verifyWith(jwtProperties.secretKey())
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
