package com.tangan.glucose.service.impl;

import com.tangan.glucose.common.*;
import com.tangan.glucose.dto.*;
import com.tangan.glucose.entity.*;
import com.tangan.glucose.repository.*;
import com.tangan.glucose.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class MedicationRecordServiceImpl implements MedicationRecordService {
    private final MedicationRecordRepository repository;
    private final UserService userService;

    @Override @Transactional(readOnly = true)
    public PageResponse<MedicationRecordDtos.Response> list(UUID userId, LocalDate from, LocalDate to, int page, int pageSize) {
        DateRange range = DateRange.of(from, to);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(pageSize, 1), 100), Sort.by(Sort.Direction.DESC, "takenAt"));
        Page<MedicationRecord> result = repository.findByUserIdAndDeletedFalseAndTakenAtBetween(userId, range.from(), range.to(), pageable);
        return new PageResponse<>(result.map(this::toResponse).getContent(), result.getNumber() + 1, result.getSize(), result.getTotalElements(), result.hasNext());
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public MedicationRecordDtos.Response create(UUID userId, MedicationRecordDtos.Request request) {
        MedicationRecord r = new MedicationRecord(); r.setUser(userService.require(userId));
        r.setMedicationName(request.medicationName()); r.setDose(request.dose()); r.setDoseUnit(request.doseUnit()); r.setTakenAt(request.takenAt()); r.setScheduledAt(request.scheduledAt()); r.setStatus(request.status()); r.setNote(request.note());
        return toResponse(repository.save(r));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public MedicationRecordDtos.Response update(UUID userId, UUID id, MedicationRecordDtos.Update request) {
        MedicationRecord r = repository.findByIdAndUserIdAndDeletedFalse(id, userId).orElseThrow(() -> ApiException.notFound("用药记录不存在"));
        if (request.medicationName() != null) r.setMedicationName(request.medicationName());
        if (request.dose() != null) r.setDose(request.dose());
        if (request.doseUnit() != null) r.setDoseUnit(request.doseUnit());
        if (request.takenAt() != null) r.setTakenAt(request.takenAt());
        if (request.scheduledAt() != null) r.setScheduledAt(request.scheduledAt());
        if (request.status() != null) r.setStatus(request.status());
        if (request.note() != null) r.setNote(request.note());
        return toResponse(repository.save(r));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, UUID id) {
        if (repository.softDelete(id, userId, OffsetDateTime.now()) == 0) throw ApiException.notFound("用药记录不存在");
    }

    private MedicationRecordDtos.Response toResponse(MedicationRecord r) {
        return new MedicationRecordDtos.Response(r.getId(), r.getMedicationName(), r.getDose(), r.getDoseUnit(), r.getTakenAt(), r.getScheduledAt(), r.getStatus(), adherence(r), r.getNote(), r.getCreatedAt(), r.getUpdatedAt());
    }
    private String adherence(MedicationRecord r) {
        if (!"taken".equals(r.getStatus()) || r.getTakenAt() == null || r.getScheduledAt() == null) return "not_applicable";
        long minutes = Math.abs(Duration.between(r.getScheduledAt(), r.getTakenAt()).toMinutes());
        return minutes <= 30 ? "on_time" : "late";
    }
}
