package com.tangan.glucose.service.impl;

import com.tangan.glucose.auth.JwtService;
import com.tangan.glucose.common.ApiException;
import com.tangan.glucose.dto.*;
import com.tangan.glucose.entity.*;
import com.tangan.glucose.repository.*;
import com.tangan.glucose.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserAccountRepository userRepository;
    private final AuthSessionRepository sessionRepository;
    private final UserSettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.tangan.glucose.config.TanganProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserServiceImpl userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionResponse register(RegisterRequest request) {
        if (userRepository.existsByAccount(request.phone())) throw ApiException.conflict("手机号或账号已注册");
        UserAccount user = new UserAccount();
        user.setName(request.name()); user.setAccount(request.phone()); user.setPhone(request.phone());
        user.setTimezone(request.timezone() == null || request.timezone().isBlank() ? "Asia/Shanghai" : request.timezone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);
        UserSetting setting = new UserSetting(); setting.setUser(user); settingRepository.save(setting);
        log.info("Registered user {}", user.getId());
        return createSession(user, "registration");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByAccountAndActiveTrue(request.account())
                .orElseThrow(() -> ApiException.unauthorized("账号或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed login for account {}", request.account());
            throw ApiException.unauthorized("账号或密码错误");
        }
        return createSession(user, request.deviceName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionResponse refresh(RefreshRequest request) {
        AuthSession old = sessionRepository.findByRefreshTokenHashAndRevokedFalse(hash(request.refreshToken()))
                .orElseThrow(() -> ApiException.unauthorized("refresh token 无效"));
        if (old.getExpiresAt().isBefore(OffsetDateTime.now())) throw ApiException.unauthorized("refresh token 已过期");
        old.setRevoked(true); sessionRepository.save(old);
        return createSession(old.getUser(), old.getDeviceName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.info("Logout requested without bearer token");
            return;
        }
        try {
            String token = authorizationHeader.substring(7).trim();
            Claims claims = jwtService.parseAccessToken(token);
            UUID userId = UUID.fromString(claims.getSubject());
            int revoked = sessionRepository.revokeAllByUserId(userId);
            if (claims.getExpiration() != null) jwtService.revokeAccessToken(token, claims.getExpiration().toInstant());
            log.info("Revoked {} session(s) for user {}", revoked, userId);
        } catch (Exception ex) {
            log.debug("Logout token could not be parsed: {}", ex.getMessage());
        }
    }

    private SessionResponse createSession(UserAccount user, String deviceName) {
        String access = jwtService.createAccessToken(user.getId(), user.getAccount());
        String refresh = randomToken();
        AuthSession session = new AuthSession(); session.setUser(user); session.setDeviceName(deviceName);
        session.setRefreshTokenHash(hash(refresh));
        session.setExpiresAt(OffsetDateTime.now().plusSeconds(properties.getRefreshTokenExpireSeconds()));
        sessionRepository.save(session);
        return new SessionResponse(session.getId().toString(), access, refresh, properties.getAccessTokenExpireSeconds(), userService.toResponse(user));
    }

    private String randomToken() {
        byte[] bytes = new byte[48]; secureRandom.nextBytes(bytes);
        return "rft_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
}
