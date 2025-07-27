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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/products").permitAll()  // Allow public GET access to products
                .requestMatchers("/products/deals").permitAll()  // Allow public access to deals
                .requestMatchers("/products/by-category/**").permitAll()  // Allow public access to category products
                .requestMatchers("/products/by-tag").permitAll()  // Allow public access to tag products
                .requestMatchers("/products/{productId}").permitAll()  // Allow public GET access to product details
                .requestMatchers("/categories/**").permitAll() // Allow public access to categories
                .requestMatchers("/").permitAll()  // Allow root endpoint
                .requestMatchers("/api/orders/**").authenticated() // Require auth for orders
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.Arrays.asList("*"));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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