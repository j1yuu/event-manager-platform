package kkashin.dev.eventmanager.config;

import kkashin.dev.eventmanager.security.customHandlers.CustomAccessDeniedHandler;
import kkashin.dev.eventmanager.security.customHandlers.CustomAuthenticationEntryPoint;
import kkashin.dev.eventmanager.security.jwt.JwtFilter;
import kkashin.dev.eventmanager.security.user.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.Argon2Password4jPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
    @Autowired
    CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    JwtFilter jwtFilter;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new Argon2Password4jPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider(
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    SecurityFilterChain filter(
            HttpSecurity httpSecurity,
            AuthenticationProvider authenticationProvider
    ) throws Exception {
        return httpSecurity
                .authenticationProvider(authenticationProvider)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        http -> http
                                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                                .requestMatchers(HttpMethod.POST, "/users/auth").permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(
                        exception -> exception
                                .accessDeniedHandler(customAccessDeniedHandler)
                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .addFilterBefore(jwtFilter, AnonymousAuthenticationFilter.class)
                .build();
    }
}
