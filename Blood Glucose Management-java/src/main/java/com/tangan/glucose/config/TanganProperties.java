package com.tangan.glucose.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "tangan")
public class TanganProperties {
    private String jwtSecret;
    private long accessTokenExpireSeconds = 3600;
    private long refreshTokenExpireSeconds = 2592000;
    private List<String> allowedOrigins = new ArrayList<>();
}
