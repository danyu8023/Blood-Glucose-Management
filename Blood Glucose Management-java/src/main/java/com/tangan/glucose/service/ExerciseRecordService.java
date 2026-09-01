package com.tangan.glucose.service;

import com.tangan.glucose.dto.*;
import java.time.LocalDate;
import java.util.UUID;

public interface ExerciseRecordService {
    PageResponse<ExerciseRecordDtos.Response> list(UUID userId, LocalDate from, LocalDate to, int page, int pageSize);
    ExerciseRecordDtos.Response create(UUID userId, ExerciseRecordDtos.Request request);
    ExerciseRecordDtos.Response update(UUID userId, UUID id, ExerciseRecordDtos.Update request);
    void delete(UUID userId, UUID id);
}
