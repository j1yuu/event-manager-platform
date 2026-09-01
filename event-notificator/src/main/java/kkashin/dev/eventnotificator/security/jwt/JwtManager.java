package kkashin.dev.eventnotificator.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;


import static kkashin.dev.securityConstants.SecurityClaims.LOGIN_CLAIM;
import static kkashin.dev.securityConstants.SecurityClaims.ROLE_CLAIM;

@Component
public class JwtManager {

    private final JwtProperties jwtProperties;

    public JwtManager(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private Claims getPayload(String jwt) {
        return Jwts.parser()
                .verifyWith(jwtProperties.secretKey())
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public String getLogin(String jwt) {
        var claims = getPayload(jwt);

        return claims.get(LOGIN_CLAIM, String.class);
    }

    public Long getId(String jwt) {
        var claims = getPayload(jwt);

        return Long.valueOf(claims.getSubject());
    }
}
