package kkashin.dev.eventmanager.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kkashin.dev.eventmanager.security.user.CustomUserDetailsService;
import kkashin.dev.eventmanager.security.user.User;
import kkashin.dev.eventmanager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final static Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtManager jwtTokenManager;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtManager jwtTokenManager, CustomUserDetailsService userDetailsService) {
        this.jwtTokenManager = jwtTokenManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            var jwt = authorization.substring(7);

            try {
                String login = jwtTokenManager.getLogin(jwt);
                User user = userDetailsService.loadUserByUsername(login);

                var authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                log.error("Error while reading jwt: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
