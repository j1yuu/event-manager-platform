package kkashin.dev.eventmanager.security.jwt;

import io.jsonwebtoken.Jwts;
import kkashin.dev.eventmanager.security.user.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

import static kkashin.dev.securityConstants.SecurityClaims.LOGIN_CLAIM;
import static kkashin.dev.securityConstants.SecurityClaims.ROLE_CLAIM;

@Component
public class JwtManager {

    private final JwtProperties jwtProperties;

    public JwtManager(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generate(User user) {
        var now = Instant.now();

        return Jwts
                .builder()
                .subject(user.getId().toString())
                .claim(LOGIN_CLAIM, user.getLoginNormalized())
                .claim(ROLE_CLAIM, user.getUserRole())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.accessTtl())))
                .signWith(jwtProperties.secretKey(), Jwts.SIG.HS256)
                .compact();
    }



    public String getLogin(String jwt) {
        return Jwts.parser()
                .verifyWith(jwtProperties.secretKey())
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .get(LOGIN_CLAIM, String.class);
    }
}
