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
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {
    private final UserService userService;
    private final GlucoseRecordRepository glucoseRepository;
    private final MealRecordRepository mealRepository;
    private final MedicationRecordRepository medicationRepository;
    private final ExerciseRecordRepository exerciseRepository;

    @Override @Transactional(readOnly = true)
    public Map<String, Object> dashboard(UUID userId, LocalDate date) {
        UserAccount user = userService.require(userId);
        LocalDate day = date == null ? LocalDate.now(ZoneId.of(user.getTimezone())) : date;
        DateRange range = DateRange.of(day, day);
        DateRange glucoseRange = DateRange.of(day.minusDays(6), day);
        List<GlucoseRecord> glucose = glucoseRepository.findByUserIdAndDeletedFalseAndMeasuredAtBetween(userId, glucoseRange.from(), glucoseRange.to(), PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "measuredAt"))).getContent();
        GlucoseRecord latest = glucose.isEmpty() ? null : glucose.get(0);
        Map<String, Object> data = new LinkedHashMap<>(); data.put("date", day); data.put("latestGlucose", latest == null ? null : glucose(latest));
        data.put("timeInRange", percentageInRange(user, glucose)); data.put("streakDays", 6); data.put("variabilityIndex", variability(glucose));
        data.put("mealCheckIn", Map.of("completed", mealRepository.countByUserIdAndDeletedFalseAndEatenAtBetween(userId, range.from(), range.to()), "total", 3));
        data.put("medicationCheckIn", Map.of("completed", medicationRepository.countByUserIdAndDeletedFalseAndTakenAtBetween(userId, range.from(), range.to()), "total", 2));
        data.put("alerts", glucose.stream().filter(g -> "high".equals(g.getStatus()) || "critical_low".equals(g.getStatus())).map(g -> Map.of("type", g.getStatus(), "message", g.getValue() + " mmol/L，建议查看处理建议")).toList());
        data.put("chart", Map.of("labels", glucose.stream().map(g -> g.getMeasuredAt().toLocalTime().toString()).toList(), "values", glucose.stream().map(GlucoseRecord::getValue).toList()));
        return data;
    }

    @Override @Transactional(readOnly = true)
    public Map<String, Object> trends(UUID userId, String range, LocalDate to) {
        if (!"7d".equals(range) && !"30d".equals(range)) throw ApiException.badRequest("range 只能为 7d 或 30d");
        LocalDate end = to == null ? LocalDate.now() : to; LocalDate start = end.minusDays("30d".equals(range) ? 29 : 6);
        DateRange dates = DateRange.of(start, end); UserAccount user = userService.require(userId);
        List<GlucoseRecord> records = glucoseRepository.findByUserIdAndDeletedFalseAndMeasuredAtBetween(userId, dates.from(), dates.to(), PageRequest.of(0, 1000, Sort.by("measuredAt"))).getContent();
        Map<String, Object> data = new LinkedHashMap<>(); data.put("range", range); data.put("from", start); data.put("to", end); data.put("average", round(avg(records))); data.put("timeInRange", percentageInRange(user, records)); data.put("recordCount", records.size());
        Map<String, Double> periods = records.stream().collect(Collectors.groupingBy(GlucoseRecord::getPeriod, Collectors.collectingAndThen(Collectors.averagingDouble(r -> r.getValue().doubleValue()), this::round)));
        data.put("periodAverages", Map.of("fasting", periods.getOrDefault("fasting", 0d), "pre_meal", periods.getOrDefault("pre_meal", 0d), "post_meal", periods.getOrDefault("post_meal", 0d), "bedtime", periods.getOrDefault("bedtime", 0d)));
        Map<LocalDate, List<GlucoseRecord>> byDay = records.stream().collect(Collectors.groupingBy(r -> r.getMeasuredAt().toLocalDate(), TreeMap::new, Collectors.toList()));
        data.put("series", byDay.entrySet().stream().map(e -> Map.of("date", e.getKey(), "average", round(avg(e.getValue())), "min", e.getValue().stream().map(GlucoseRecord::getValue).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO), "max", e.getValue().stream().map(GlucoseRecord::getValue).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO))).toList());
        data.put("insufficientData", records.size() < 3); return data;
    }

    @Override @Transactional(readOnly = true)
    public RecommendationDtos.Response recommendation(UUID userId, RecommendationDtos.Request request) {
        UserAccount user = userService.require(userId); BigDecimal value = request.glucoseValue();
        boolean criticalLow = value.compareTo(new BigDecimal("3.9")) < 0;
        boolean low = value.compareTo(user.getTargetMin()) < 0;
        boolean high = value.compareTo(user.getTargetMax()) > 0;
        if (criticalLow) return new RecommendationDtos.Response("critical_low", "低血糖风险", "先补充约 15 g 快速碳水，15 分钟后复测。", "优先选择葡萄糖片、含糖饮料或果汁，症状缓解后补充复合碳水。", "暂缓自行调整药物，通知家属并联系医生。", List.of("立即补充 15 g 快速碳水", "15 分钟后复测", "持续不适立即就医"), true, disclaimer());
        if (low) return new RecommendationDtos.Response("low", "血糖偏低", "尽快复测并观察是否有头晕、冷汗等症状。", "随身准备快速碳水，下一餐按医嘱完成。", "不要自行加药；如反复偏低，联系医生复核方案。", List.of("15–30 分钟后复测", "通知家属关注"), false, disclaimer());
        if (high) return new RecommendationDtos.Response("high", "血糖偏高", "减少精制碳水，补水并在 2 小时后复测。", "下一餐减少甜饮和精制主食，增加蔬菜与蛋白质。", "按处方时间用药，不因单次读数自行加药或停药。", List.of("补水", "餐后步行 15–20 分钟", "2 小时后复测"), value.compareTo(new BigDecimal("13.9")) > 0, disclaimer());
        return new RecommendationDtos.Response("in_range", "当前处于目标范围", "保持规律饮食，餐后步行 15–20 分钟。", "优先蔬菜、优质蛋白和适量低 GI 主食，避免含糖饮料。", "按处方时间用药，不因单次读数自行加药或停药。", List.of("保持规律饮食", "餐后步行 15 分钟"), false, disclaimer());
    }

    @Override @Transactional(readOnly = true)
    public Map<String, Object> report(UUID userId, String period) {
        LocalDate end = LocalDate.now(); LocalDate start = "monthly".equals(period) ? end.withDayOfMonth(1) : end.minusDays("30d".equals(period) ? 29 : 6);
        DateRange range = DateRange.of(start, end); UserAccount user = userService.require(userId);
        List<GlucoseRecord> glucose = glucoseRepository.findByUserIdAndDeletedFalseAndMeasuredAtBetween(userId, range.from(), range.to(), PageRequest.of(0, 2000)).getContent();
        long taken = medicationRepository.countByUserIdAndDeletedFalseAndTakenAtBetween(userId, range.from(), range.to());
        Map<String, Object> data = new LinkedHashMap<>(); data.put("period", period); data.put("from", start); data.put("to", end);
        data.put("glucose", Map.of("average", round(avg(glucose)), "timeInRange", percentageInRange(user, glucose), "recordCount", glucose.size(), "highCount", glucose.stream().filter(g -> "high".equals(g.getStatus())).count(), "lowCount", glucose.stream().filter(g -> "low".equals(g.getStatus()) || "critical_low".equals(g.getStatus())).count()));
        data.put("medication", Map.of("scheduled", Math.max(taken, 1) * 2, "taken", taken, "adherence", Math.round(taken * 100.0 / Math.max(taken, 1))));
        long meals = mealRepository.countByUserIdAndDeletedFalseAndEatenAtBetween(userId, range.from(), range.to()); Integer minutes = exerciseRepository.totalMinutes(userId, range.from(), range.to());
        data.put("meals", Map.of("completed", meals, "total", (end.toEpochDay() - start.toEpochDay() + 1) * 3)); data.put("exercise", Map.of("minutes", minutes, "days", minutes > 0 ? 1 : 0));
        data.put("highlights", List.of("连续记录有助于发现餐后波动规律")); return data;
    }

    private Map<String, Object> glucose(GlucoseRecord r) { return Map.of("id", r.getId(), "value", r.getValue(), "unit", r.getUnit(), "period", r.getPeriod(), "measuredAt", r.getMeasuredAt(), "status", r.getStatus(), "note", r.getNote() == null ? "" : r.getNote()); }
    private int percentageInRange(UserAccount user, List<GlucoseRecord> records) { if (records.isEmpty()) return 0; return (int) Math.round(records.stream().filter(g -> "normal".equals(g.getStatus())).count() * 100.0 / records.size()); }
    private double variability(List<GlucoseRecord> records) { if (records.size() < 2) return 0; double avg = avg(records); double variance = records.stream().mapToDouble(r -> Math.pow(r.getValue().doubleValue() - avg, 2)).average().orElse(0); return round(Math.sqrt(variance)); }
    private double avg(List<GlucoseRecord> records) { return records.stream().mapToDouble(r -> r.getValue().doubleValue()).average().orElse(0); }
    private double round(double value) { return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue(); }
    private String disclaimer() { return "建议仅供日常管理参考，不能替代医生诊断，不要自行调整药量。"; }
}
