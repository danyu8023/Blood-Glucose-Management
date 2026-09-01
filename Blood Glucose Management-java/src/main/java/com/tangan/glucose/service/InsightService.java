package com.tangan.glucose.service;

import com.tangan.glucose.dto.RecommendationDtos;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface InsightService {
    Map<String, Object> dashboard(UUID userId, LocalDate date);
    Map<String, Object> trends(UUID userId, String range, LocalDate to);
    RecommendationDtos.Response recommendation(UUID userId, RecommendationDtos.Request request);
    Map<String, Object> report(UUID userId, String period);
}
