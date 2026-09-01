package com.tangan.glucose.controller;

import com.tangan.glucose.common.*;
import com.tangan.glucose.dto.*;
import com.tangan.glucose.service.GlucoseRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/glucose-records") @RequiredArgsConstructor
public class GlucoseRecordController {
    private final GlucoseRecordService service;
    @GetMapping public Result<PageResponse<GlucoseRecordDtos.Response>> list(@RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to, @RequestParam(required = false) String period, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) { return Result.success(service.list(AuthContext.requireUserId(), from, to, period, page, pageSize)); }
    @GetMapping("/{id}") public Result<GlucoseRecordDtos.Response> get(@PathVariable UUID id) { return Result.success(service.get(AuthContext.requireUserId(), id)); }
    @PostMapping public ResponseEntity<Result<GlucoseRecordDtos.Response>> create(@Valid @RequestBody GlucoseRecordDtos.Request request) { return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(service.create(AuthContext.requireUserId(), request))); }
    @PatchMapping("/{id}") public Result<GlucoseRecordDtos.Response> update(@PathVariable UUID id, @Valid @RequestBody GlucoseRecordDtos.Update request) { return Result.success(service.update(AuthContext.requireUserId(), id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(AuthContext.requireUserId(), id); return ResponseEntity.noContent().build(); }
}
