package com.mobiledev.emporio.security;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BasicAuthenticationFilter rateLimitingFilter() {
        return new BasicAuthenticationFilter(authenticationManager -> null) {
            private final Map<String, AtomicInteger> attempts = new ConcurrentHashMap<>();
            private final int MAX_ATTEMPTS = 10;
            private final long WINDOW_MS = 60_000; // 1 minute
            private final Map<String, Long> firstAttemptTime = new ConcurrentHashMap<>();
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                String ip = request.getRemoteAddr();
                long now = System.currentTimeMillis();
                firstAttemptTime.putIfAbsent(ip, now);
                if (now - firstAttemptTime.get(ip) > WINDOW_MS) {
                    attempts.put(ip, new AtomicInteger(1));
                    firstAttemptTime.put(ip, now);
                } else {
                    int count = attempts.computeIfAbsent(ip, k -> new AtomicInteger(0)).incrementAndGet();
                    if (count > MAX_ATTEMPTS) {
                        response.setStatus(429);
                        response.getWriter().write("Too many requests. Please try again later.");
                        return;
                    }
                }
                chain.doFilter(request, response);
            }
        };
    }
}