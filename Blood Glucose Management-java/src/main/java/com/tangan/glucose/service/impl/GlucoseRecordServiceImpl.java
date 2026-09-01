package com.tangan.glucose.service.impl;

import com.tangan.glucose.common.*;
import com.tangan.glucose.dto.*;
import com.tangan.glucose.entity.*;
import com.tangan.glucose.repository.*;
import com.tangan.glucose.service.GlucoseRecordService;
import com.tangan.glucose.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlucoseRecordServiceImpl implements GlucoseRecordService {
    private final GlucoseRecordRepository repository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GlucoseRecordDtos.Response> list(UUID userId, LocalDate from, LocalDate to, String period, int page, int pageSize) {
        DateRange range = DateRange.of(from, to);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(pageSize, 1), 100), Sort.by(Sort.Direction.DESC, "measuredAt"));
        Page<GlucoseRecord> result = period == null || period.isBlank()
                ? repository.findByUserIdAndDeletedFalseAndMeasuredAtBetween(userId, range.from(), range.to(), pageable)
                : repository.findByUserIdAndDeletedFalseAndMeasuredAtBetweenAndPeriod(userId, range.from(), range.to(), period, pageable);
        return new PageResponse<>(result.map(this::toResponse).getContent(), result.getNumber() + 1, result.getSize(), result.getTotalElements(), result.hasNext());
    }

    @Override @Transactional(readOnly = true)
    public GlucoseRecordDtos.Response get(UUID userId, UUID id) {
        return toResponse(repository.findByIdAndUserIdAndDeletedFalse(id, userId).orElseThrow(() -> ApiException.notFound("血糖记录不存在")));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public GlucoseRecordDtos.Response create(UUID userId, GlucoseRecordDtos.Request request) {
        validateMeasuredAt(request.measuredAt());
        UserAccount user = userService.require(userId);
        GlucoseRecord record = new GlucoseRecord(); record.setUser(user); record.setValue(request.value());
        record.setUnit(request.unit() == null ? "mmol/L" : request.unit()); record.setPeriod(request.period());
        record.setMeasuredAt(request.measuredAt()); record.setNote(request.note()); record.setStatus(status(record.getValue(), user));
        GlucoseRecord saved = repository.save(record);
        log.info("Created glucose record {} for user {}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public GlucoseRecordDtos.Response update(UUID userId, UUID id, GlucoseRecordDtos.Update request) {
        GlucoseRecord record = repository.findByIdAndUserIdAndDeletedFalse(id, userId).orElseThrow(() -> ApiException.notFound("血糖记录不存在"));
        UserAccount user = record.getUser();
        if (request.value() != null) record.setValue(request.value());
        if (request.unit() != null) record.setUnit(request.unit());
        if (request.period() != null) record.setPeriod(request.period());
        if (request.measuredAt() != null) { validateMeasuredAt(request.measuredAt()); record.setMeasuredAt(request.measuredAt()); }
        if (request.note() != null) record.setNote(request.note());
        record.setStatus(status(record.getValue(), user));
        return toResponse(repository.save(record));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, UUID id) {
        if (repository.softDelete(id, userId, OffsetDateTime.now()) == 0) throw ApiException.notFound("血糖记录不存在");
        log.info("Soft deleted glucose record {} for user {}", id, userId);
    }

    private void validateMeasuredAt(OffsetDateTime measuredAt) {
        if (measuredAt.isAfter(OffsetDateTime.now().plusMinutes(5)) || measuredAt.isBefore(OffsetDateTime.now().minusYears(2))) {
            throw ApiException.badRequest("测量时间超出允许范围");
        }
    }

    public String status(BigDecimal value, UserAccount user) {
        if (value.compareTo(new BigDecimal("3.9")) < 0) return "critical_low";
        if (value.compareTo(user.getTargetMin()) < 0) return "low";
        if (value.compareTo(user.getTargetMax()) > 0) return "high";
        return "normal";
    }

    private GlucoseRecordDtos.Response toResponse(GlucoseRecord r) {
        return new GlucoseRecordDtos.Response(r.getId(), r.getValue(), r.getUnit(), r.getPeriod(), r.getMeasuredAt(), r.getStatus(), r.getNote(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
