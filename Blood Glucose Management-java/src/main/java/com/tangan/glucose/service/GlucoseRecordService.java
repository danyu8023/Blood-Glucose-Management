package com.tangan.glucose.service;

import com.tangan.glucose.dto.*;
import java.time.LocalDate;
import java.util.UUID;

public interface GlucoseRecordService {
    PageResponse<GlucoseRecordDtos.Response> list(UUID userId, LocalDate from, LocalDate to, String period, int page, int pageSize);
    GlucoseRecordDtos.Response get(UUID userId, UUID id);
    GlucoseRecordDtos.Response create(UUID userId, GlucoseRecordDtos.Request request);
    GlucoseRecordDtos.Response update(UUID userId, UUID id, GlucoseRecordDtos.Update request);
    void delete(UUID userId, UUID id);
}
