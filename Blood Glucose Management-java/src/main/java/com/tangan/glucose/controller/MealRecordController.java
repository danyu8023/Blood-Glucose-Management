package com.tangan.glucose.controller;

import com.tangan.glucose.common.*;
import com.tangan.glucose.dto.*;
import com.tangan.glucose.service.MealRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/meal-records") @RequiredArgsConstructor
public class MealRecordController {
    private final MealRecordService service;
    @GetMapping public Result<PageResponse<MealRecordDtos.Response>> list(@RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to, @RequestParam(required = false) LocalDate date, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) { LocalDate start = date == null ? from : date; return Result.success(service.list(AuthContext.requireUserId(), start, date == null ? to : date, page, pageSize)); }
    @PostMapping public ResponseEntity<Result<MealRecordDtos.Response>> create(@Valid @RequestBody MealRecordDtos.Request request) { return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(service.create(AuthContext.requireUserId(), request))); }
    @PatchMapping("/{id}") public Result<MealRecordDtos.Response> update(@PathVariable UUID id, @Valid @RequestBody MealRecordDtos.Update request) { return Result.success(service.update(AuthContext.requireUserId(), id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(AuthContext.requireUserId(), id); return ResponseEntity.noContent().build(); }
}
