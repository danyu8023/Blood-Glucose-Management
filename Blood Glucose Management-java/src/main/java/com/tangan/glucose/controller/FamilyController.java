package com.tangan.glucose.controller;

import com.tangan.glucose.common.*;
import com.tangan.glucose.dto.FamilyDtos;
import com.tangan.glucose.service.FamilyConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/family-connections") @RequiredArgsConstructor
public class FamilyController {
    private final FamilyConnectionService service;
    @GetMapping public Result<List<FamilyDtos.Response>> list() { return Result.success(service.list(AuthContext.requireUserId())); }
    @PostMapping public ResponseEntity<Result<FamilyDtos.Response>> create(@Valid @RequestBody FamilyDtos.Request request) { return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(service.create(AuthContext.requireUserId(), request))); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(AuthContext.requireUserId(), id); return ResponseEntity.noContent().build(); }
}
