package com.saas.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS,HEAD}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers:Authorization,Content-Disposition,Link,X-Total-Count}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private Long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = parseList(allowedOrigins);
        if (origins.isEmpty()) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOriginPatterns(origins);
        }

        List<String> methods = parseList(allowedMethods);
        if (methods.isEmpty()) {
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        } else {
            config.setAllowedMethods(methods);
        }

        List<String> headers = parseList(allowedHeaders);
        if (headers.isEmpty()) {
            config.setAllowedHeaders(List.of("*"));
        } else {
            config.setAllowedHeaders(headers);
        }

        List<String> exposed = parseList(exposedHeaders);
        if (!exposed.isEmpty()) {
            config.setExposedHeaders(exposed);
        }

        config.setAllowCredentials(allowCredentials);
        if (maxAge != null) {
            config.setMaxAge(maxAge);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    private List<String> parseList(String commaSeparatedValues) {
        if (commaSeparatedValues == null || commaSeparatedValues.isBlank()) {
            return List.of();
        }
        return Arrays.stream(commaSeparatedValues.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
