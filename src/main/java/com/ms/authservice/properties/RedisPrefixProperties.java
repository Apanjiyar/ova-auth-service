package com.ms.authservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "redis.keys")
public record RedisPrefixProperties(
        String blacklistToken,
        String userInfo
) {}
