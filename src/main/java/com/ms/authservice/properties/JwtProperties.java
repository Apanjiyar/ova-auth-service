package com.ms.authservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        Long expirationTimeMs,
        String refreshToken,
        Long refreshExpirationTimeMs
) {}