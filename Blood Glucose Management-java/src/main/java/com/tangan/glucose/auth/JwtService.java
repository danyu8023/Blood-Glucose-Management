package com.tangan.glucose.auth;

import com.tangan.glucose.config.TanganProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final TanganProperties properties;
    private final ConcurrentHashMap<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    private SecretKey key() {
        return Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(UUID userId, String account) {
        Instant now = Instant.now();
        return Jwts.builder().subject(userId.toString()).claim("account", account).claim("type", "access")
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(properties.getAccessTokenExpireSeconds())))
                .signWith(key()).compact();
    }

    public Claims parseAccessToken(String token) {
        Instant revokedUntil = revokedTokens.get(token);
        if (revokedUntil != null) {
            if (revokedUntil.isAfter(Instant.now())) throw new IllegalArgumentException("token revoked");
            revokedTokens.remove(token);
        }
        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        if (!"access".equals(claims.get("type", String.class))) throw new IllegalArgumentException("not access token");
        return claims;
    }

    public void revokeAccessToken(String token, Instant expiresAt) {
        revokedTokens.put(token, expiresAt);
    }
}
