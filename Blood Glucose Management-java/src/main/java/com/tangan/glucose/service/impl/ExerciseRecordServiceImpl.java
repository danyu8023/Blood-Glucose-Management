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
import java.util.*;

@Slf4j @Service @RequiredArgsConstructor
public class ExerciseRecordServiceImpl implements ExerciseRecordService {
    private final ExerciseRecordRepository repository;
    private final UserService userService;

    @Override @Transactional(readOnly = true)
    public PageResponse<ExerciseRecordDtos.Response> list(UUID userId, LocalDate from, LocalDate to, int page, int pageSize) {
        DateRange range = DateRange.of(from, to);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(pageSize, 1), 100), Sort.by(Sort.Direction.DESC, "startedAt"));
        Page<ExerciseRecord> result = repository.findByUserIdAndDeletedFalseAndStartedAtBetween(userId, range.from(), range.to(), pageable);
        return new PageResponse<>(result.map(this::toResponse).getContent(), result.getNumber() + 1, result.getSize(), result.getTotalElements(), result.hasNext());
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public ExerciseRecordDtos.Response create(UUID userId, ExerciseRecordDtos.Request request) {
        ExerciseRecord r = new ExerciseRecord(); r.setUser(userService.require(userId));
        r.setExerciseType(request.exerciseType()); r.setStartedAt(request.startedAt()); r.setDurationMinutes(request.durationMinutes()); r.setIntensity(request.intensity()); r.setBeforeGlucose(request.beforeGlucose()); r.setAfterGlucose(request.afterGlucose()); r.setNote(request.note());
        return toResponse(repository.save(r));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public ExerciseRecordDtos.Response update(UUID userId, UUID id, ExerciseRecordDtos.Update request) {
        ExerciseRecord r = repository.findByIdAndUserIdAndDeletedFalse(id, userId).orElseThrow(() -> ApiException.notFound("运动记录不存在"));
        if (request.exerciseType() != null) r.setExerciseType(request.exerciseType());
        if (request.startedAt() != null) r.setStartedAt(request.startedAt());
        if (request.durationMinutes() != null) r.setDurationMinutes(request.durationMinutes());
        if (request.intensity() != null) r.setIntensity(request.intensity());
        if (request.beforeGlucose() != null) r.setBeforeGlucose(request.beforeGlucose());
        if (request.afterGlucose() != null) r.setAfterGlucose(request.afterGlucose());
        if (request.note() != null) r.setNote(request.note());
        return toResponse(repository.save(r));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, UUID id) {
        if (repository.softDelete(id, userId, OffsetDateTime.now()) == 0) throw ApiException.notFound("运动记录不存在");
    }

    private ExerciseRecordDtos.Response toResponse(ExerciseRecord r) {
        return new ExerciseRecordDtos.Response(r.getId(), r.getExerciseType(), r.getStartedAt(), r.getDurationMinutes(), r.getIntensity(), r.getBeforeGlucose(), r.getAfterGlucose(), r.getNote(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
