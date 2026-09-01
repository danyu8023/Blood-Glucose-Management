package com.tangan.glucose.controller;

import com.tangan.glucose.common.*;
import com.tangan.glucose.dto.*;
import com.tangan.glucose.service.ExerciseRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/exercise-records") @RequiredArgsConstructor
public class ExerciseRecordController {
    private final ExerciseRecordService service;
    @GetMapping public Result<PageResponse<ExerciseRecordDtos.Response>> list(@RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) { return Result.success(service.list(AuthContext.requireUserId(), from, to, page, pageSize)); }
    @PostMapping public ResponseEntity<Result<ExerciseRecordDtos.Response>> create(@Valid @RequestBody ExerciseRecordDtos.Request request) { return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(service.create(AuthContext.requireUserId(), request))); }
    @PatchMapping("/{id}") public Result<ExerciseRecordDtos.Response> update(@PathVariable UUID id, @Valid @RequestBody ExerciseRecordDtos.Update request) { return Result.success(service.update(AuthContext.requireUserId(), id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(AuthContext.requireUserId(), id); return ResponseEntity.noContent().build(); }
}
