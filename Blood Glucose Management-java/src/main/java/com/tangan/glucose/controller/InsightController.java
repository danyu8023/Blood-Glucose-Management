package com.tangan.glucose.controller;

import com.tangan.glucose.common.AuthContext;
import com.tangan.glucose.common.Result;
import com.tangan.glucose.dto.RecommendationDtos;
import com.tangan.glucose.service.InsightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController @RequestMapping("/api/v1") @RequiredArgsConstructor
public class InsightController {
    private final InsightService service;
    @GetMapping("/dashboard") public Result<Map<String, Object>> dashboard(@RequestParam(required = false) LocalDate date) { return Result.success(service.dashboard(AuthContext.requireUserId(), date)); }
    @GetMapping("/glucose-trends") public Result<Map<String, Object>> trends(@RequestParam(defaultValue = "7d") String range, @RequestParam(required = false) LocalDate to) { return Result.success(service.trends(AuthContext.requireUserId(), range, to)); }
    @PostMapping("/recommendations") public Result<RecommendationDtos.Response> recommendation(@Valid @RequestBody RecommendationDtos.Request request) { return Result.success(service.recommendation(AuthContext.requireUserId(), request)); }
    @GetMapping("/reports/{period}") public Result<Map<String, Object>> report(@PathVariable String period) { return Result.success(service.report(AuthContext.requireUserId(), period)); }
}
