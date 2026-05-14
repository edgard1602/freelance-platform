package com.freelance.freelance_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        JwtProperties jwt,
        CorsProperties cors,
        PaginationProperties pagination
) {
    public record JwtProperties(
            String secret,
            @DefaultValue("900") long accessTokenExpiration,
            @DefaultValue("604800") long refreshTokenExpiration,
            @DefaultValue("freelance-platform") String issuer
    ) {}

    public record CorsProperties(
            @DefaultValue("http://localhost:4200") List<String> allowedOrigins,
            @DefaultValue("GET,POST,PUT,PATCH,DELETE,OPTIONS") List<String> allowedMethods,
            @DefaultValue("*") List<String> allowedHeaders,
            @DefaultValue("true") boolean allowCredentials,
            @DefaultValue("3600") long maxAge
    ) {}

    public record PaginationProperties(
            @DefaultValue("20") int defaultPageSize,
            @DefaultValue("100") int maxPageSize
    ) {}
}
