package com.tangan.glucose.dto;

public record SessionResponse(String sessionId, String accessToken, String refreshToken, long expiresIn, UserResponse user) { }
