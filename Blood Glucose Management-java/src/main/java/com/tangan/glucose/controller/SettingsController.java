package com.tangan.glucose.controller;

import com.tangan.glucose.common.*;
import com.tangan.glucose.dto.SettingsDtos;
import com.tangan.glucose.service.SettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/me/settings") @RequiredArgsConstructor
public class SettingsController {
    private final SettingService service;
    @GetMapping public Result<SettingsDtos.Response> get() { return Result.success(service.get(AuthContext.requireUserId())); }
    @PatchMapping public Result<SettingsDtos.Response> update(@Valid @RequestBody SettingsDtos.Request request) { return Result.success(service.update(AuthContext.requireUserId(), request)); }
}
