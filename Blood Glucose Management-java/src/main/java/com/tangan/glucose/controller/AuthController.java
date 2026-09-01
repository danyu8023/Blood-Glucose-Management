package com.tangan.glucose.controller;

import com.tangan.glucose.common.Result;
import com.tangan.glucose.dto.*;
import com.tangan.glucose.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/users")
    public ResponseEntity<Result<SessionResponse>> register(@Valid @RequestBody RegisterRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(service.register(request))); }

    @PostMapping("/sessions")
    public ResponseEntity<Result<SessionResponse>> login(@Valid @RequestBody LoginRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(service.login(request))); }

    @PostMapping("/sessions/refresh")
    public Result<SessionResponse> refresh(@Valid @RequestBody RefreshRequest request) { return Result.success(service.refresh(request)); }

    @DeleteMapping("/sessions/current")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) { service.logout(authorization); return ResponseEntity.noContent().build(); }
}
