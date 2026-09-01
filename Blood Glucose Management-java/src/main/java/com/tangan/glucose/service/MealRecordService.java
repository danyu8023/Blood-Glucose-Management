package com.tangan.glucose.service;

import com.tangan.glucose.dto.*;
import java.time.LocalDate;
import java.util.UUID;

public interface MealRecordService {
    PageResponse<MealRecordDtos.Response> list(UUID userId, LocalDate from, LocalDate to, int page, int pageSize);
    MealRecordDtos.Response create(UUID userId, MealRecordDtos.Request request);
    MealRecordDtos.Response update(UUID userId, UUID id, MealRecordDtos.Update request);
    void delete(UUID userId, UUID id);
}
