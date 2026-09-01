package com.tangan.glucose.service;

import com.tangan.glucose.dto.*;
import java.time.LocalDate;
import java.util.UUID;

public interface MedicationRecordService {
    PageResponse<MedicationRecordDtos.Response> list(UUID userId, LocalDate from, LocalDate to, int page, int pageSize);
    MedicationRecordDtos.Response create(UUID userId, MedicationRecordDtos.Request request);
    MedicationRecordDtos.Response update(UUID userId, UUID id, MedicationRecordDtos.Update request);
    void delete(UUID userId, UUID id);
}
