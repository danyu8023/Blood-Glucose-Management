package com.tangan.glucose.controller;

import com.tangan.glucose.common.Result;
import com.tangan.glucose.dto.PageResponse;
import com.tangan.glucose.service.PublicContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/v1/public") @RequiredArgsConstructor
public class PublicContentController {
    private final PublicContentService service;
    @GetMapping("/articles") public Result<PageResponse<Map<String, Object>>> articles(@RequestParam(required = false) String category, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) { return Result.success(service.articles(category, page, pageSize)); }
    @GetMapping("/articles/{slug}") public Result<Map<String, Object>> article(@PathVariable String slug) { return Result.success(service.article(slug)); }
    @GetMapping("/guides/{slug}") public Result<Map<String, Object>> guide(@PathVariable String slug) { return Result.success(service.guide(slug)); }
}
