package com.tangan.glucose.controller;

import com.tangan.glucose.common.*;
import com.tangan.glucose.dto.*;
import com.tangan.glucose.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/me") @RequiredArgsConstructor
public class ProfileController {
    private final UserService service;
    @GetMapping public Result<UserResponse> me() { return Result.success(service.toResponse(service.require(AuthContext.requireUserId()))); }
    @PatchMapping public Result<UserResponse> update(@Valid @RequestBody UpdateUserRequest request) { return Result.success(service.update(AuthContext.requireUserId(), request)); }
}
