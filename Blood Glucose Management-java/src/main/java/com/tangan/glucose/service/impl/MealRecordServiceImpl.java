package com.tangan.glucose.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class MealRecordServiceImpl implements MealRecordService {
    private final MealRecordRepository repository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<MealRecordDtos.Response> list(UUID userId, LocalDate from, LocalDate to, int page, int pageSize) {
        DateRange range = DateRange.of(from, to);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(Math.max(pageSize, 1), 100), Sort.by(Sort.Direction.DESC, "eatenAt"));
        Page<MealRecord> result = repository.findByUserIdAndDeletedFalseAndEatenAtBetween(userId, range.from(), range.to(), pageable);
        return new PageResponse<>(result.map(this::toResponse).getContent(), result.getNumber() + 1, result.getSize(), result.getTotalElements(), result.hasNext());
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public MealRecordDtos.Response create(UUID userId, MealRecordDtos.Request request) {
        DateRange day = DateRange.of(request.eatenAt().toLocalDate(), request.eatenAt().toLocalDate());
        if (repository.existsByUserIdAndMealTypeAndDeletedFalseAndEatenAtBetween(userId, request.mealType(), day.from(), day.to())) {
            throw ApiException.conflict("同一天同一餐次已经记录，请使用修改接口");
        }
        MealRecord record = new MealRecord(); record.setUser(userService.require(userId)); record.setMealType(request.mealType());
        record.setEatenAt(request.eatenAt()); record.setFoodsJson(writeFoods(request.foods())); record.setCarbohydrateGrams(request.carbohydrateGrams()); record.setNote(request.note());
        return toResponse(repository.save(record));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public MealRecordDtos.Response update(UUID userId, UUID id, MealRecordDtos.Update request) {
        MealRecord record = repository.findByIdAndUserIdAndDeletedFalse(id, userId).orElseThrow(() -> ApiException.notFound("饮食记录不存在"));
        if (request.mealType() != null) record.setMealType(request.mealType());
        if (request.eatenAt() != null) record.setEatenAt(request.eatenAt());
        if (request.foods() != null) record.setFoodsJson(writeFoods(request.foods()));
        if (request.carbohydrateGrams() != null) record.setCarbohydrateGrams(request.carbohydrateGrams());
        if (request.note() != null) record.setNote(request.note());
        return toResponse(repository.save(record));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void delete(UUID userId, UUID id) {
        if (repository.softDelete(id, userId, OffsetDateTime.now()) == 0) throw ApiException.notFound("饮食记录不存在");
    }

    private String writeFoods(List<MealRecordDtos.Food> foods) {
        try { return objectMapper.writeValueAsString(foods); }
        catch (JsonProcessingException ex) { throw ApiException.badRequest("foods 格式无效"); }
    }
    private List<MealRecordDtos.Food> readFoods(String json) {
        try { return objectMapper.readValue(json, new TypeReference<>() { }); }
        catch (Exception ex) { return List.of(); }
    }
    private MealRecordDtos.Response toResponse(MealRecord r) {
        return new MealRecordDtos.Response(r.getId(), r.getMealType(), r.getEatenAt(), readFoods(r.getFoodsJson()), r.getCarbohydrateGrams(), r.getNote(), null, r.getCreatedAt(), r.getUpdatedAt());
    }
}
