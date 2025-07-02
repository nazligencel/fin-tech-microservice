package com.transaction_service.transaction_service.config.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;


import java.time.Duration;
@ConfigurationProperties(prefix = "app.jwt")
public record JwtConfig(
        @NotBlank
        String secret,
        @NotNull
        Duration expiration) {
}
