package com.tangan.glucose.service;

import com.tangan.glucose.dto.*;

public interface AuthService {
    SessionResponse register(RegisterRequest request);
    SessionResponse login(LoginRequest request);
    SessionResponse refresh(RefreshRequest request);
    void logout(String authorizationHeader);
}
