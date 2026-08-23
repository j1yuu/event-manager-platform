package kkashin.dev.eventmanager.security.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtManager {

    private final JwtProperties jwtProperties;

    public JwtManager(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generate(String login) {
        return Jwts
                .builder()
                .subject(login)
                .issuer(jwtProperties.issuer())
                .signWith(jwtProperties.secretKey(), Jwts.SIG.HS256)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.accessTtl()))
                .compact();
    }

    public String getLogin(String jwt) {
        return Jwts.parser()
                .verifyWith(jwtProperties.secretKey())
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .getSubject();
    }
}
