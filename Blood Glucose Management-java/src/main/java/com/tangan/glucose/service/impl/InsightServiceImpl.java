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
        long glucoseToday = glucoseRepository.countByUserIdAndDeletedFalseAndMeasuredAtBetween(userId, range.from(), range.to());
        long mealsToday = mealRepository.countByUserIdAndDeletedFalseAndEatenAtBetween(userId, range.from(), range.to());
        long medicationToday = medicationRepository.countByUserIdAndDeletedFalseAndTakenAtBetween(userId, range.from(), range.to());
        long exerciseToday = exerciseRepository.countByUserIdAndDeletedFalseAndStartedAtBetween(userId, range.from(), range.to());
        data.put("glucoseCheckIn", Map.of("completed", glucoseToday, "total", 1));
        data.put("mealCheckIn", Map.of("completed", mealsToday, "total", 3));
        data.put("medicationCheckIn", Map.of("completed", medicationToday, "total", 2));
        data.put("exerciseCheckIn", Map.of("completed", exerciseToday, "total", 1));
        data.put("completion", Map.of("glucose", glucoseToday > 0, "meals", mealsToday > 0, "medication", medicationToday > 0, "exercise", exerciseToday > 0, "completed", List.of(glucoseToday > 0, mealsToday > 0, medicationToday > 0, exerciseToday > 0).stream().filter(Boolean::booleanValue).count(), "total", 4));
        data.put("recentGlucose", glucose.stream().limit(20).map(this::glucose).toList());
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
        if (!Set.of("7d", "30d", "monthly").contains(period)) throw ApiException.badRequest("period 只能为 7d、30d 或 monthly");
        UserAccount user = userService.require(userId);
        ZoneId zone = ZoneId.of(user.getTimezone());
        LocalDate end = LocalDate.now(zone);
        LocalDate start = "monthly".equals(period) ? end.withDayOfMonth(1) : end.minusDays("30d".equals(period) ? 29 : 6);
        int days = (int) (end.toEpochDay() - start.toEpochDay() + 1);
        DateRange range = DateRange.of(start, end);

        List<GlucoseRecord> glucose = glucoseRepository.findByUserIdAndDeletedFalseAndMeasuredAtBetween(userId, range.from(), range.to(), PageRequest.of(0, 5000, Sort.by("measuredAt"))).getContent();
        List<MedicationRecord> medications = medicationRepository.findForReport(userId, range.from(), range.to());
        List<MealRecord> meals = mealRepository.findByUserIdAndDeletedFalseAndEatenAtBetween(userId, range.from(), range.to(), PageRequest.of(0, 5000, Sort.by("eatenAt"))).getContent();
        List<ExerciseRecord> exercises = exerciseRepository.findByUserIdAndDeletedFalseAndStartedAtBetween(userId, range.from(), range.to(), PageRequest.of(0, 5000, Sort.by("startedAt"))).getContent();

        long normalCount = glucose.stream().filter(g -> inTarget(user, g)).count();
        long highCount = glucose.stream().filter(g -> g.getValue().compareTo(user.getTargetMax()) > 0).count();
        long lowCount = glucose.stream().filter(g -> g.getValue().compareTo(user.getTargetMin()) < 0).count();
        long glucoseDays = glucose.stream().map(g -> g.getMeasuredAt().atZoneSameInstant(zone).toLocalDate()).distinct().count();
        Map<LocalDate, List<GlucoseRecord>> glucoseByDay = glucose.stream().collect(Collectors.groupingBy(g -> g.getMeasuredAt().atZoneSameInstant(zone).toLocalDate(), TreeMap::new, Collectors.toList()));

        long taken = medications.stream().filter(m -> "taken".equals(m.getStatus())).count();
        long missed = medications.stream().filter(m -> "missed".equals(m.getStatus())).count();
        long skipped = medications.stream().filter(m -> "skipped".equals(m.getStatus())).count();
        long onTime = medications.stream().filter(this::takenOnTime).count();
        int adherence = medications.isEmpty() ? 0 : (int) Math.round(taken * 100.0 / medications.size());

        long mealDays = meals.stream().map(m -> m.getEatenAt().atZoneSameInstant(zone).toLocalDate()).distinct().count();
        double totalCarbohydrate = meals.stream().map(MealRecord::getCarbohydrateGrams).filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).sum();
        long mealsWithCarbohydrate = meals.stream().filter(m -> m.getCarbohydrateGrams() != null).count();
        int exerciseMinutes = exercises.stream().mapToInt(ExerciseRecord::getDurationMinutes).sum();
        long exerciseDays = exercises.stream().map(e -> e.getStartedAt().atZoneSameInstant(zone).toLocalDate()).distinct().count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("period", period); data.put("from", start); data.put("to", end); data.put("days", days); data.put("generatedAt", OffsetDateTime.now(zone));
        data.put("glucose", Map.ofEntries(
                Map.entry("average", round(avg(glucose))), Map.entry("minimum", round(glucose.stream().mapToDouble(g -> g.getValue().doubleValue()).min().orElse(0))), Map.entry("maximum", round(glucose.stream().mapToDouble(g -> g.getValue().doubleValue()).max().orElse(0))),
                Map.entry("timeInRange", glucose.isEmpty() ? 0 : (int) Math.round(normalCount * 100.0 / glucose.size())), Map.entry("recordCount", glucose.size()), Map.entry("daysRecorded", glucoseDays),
                Map.entry("normalCount", normalCount), Map.entry("highCount", highCount), Map.entry("lowCount", lowCount),
                Map.entry("targetMin", user.getTargetMin()), Map.entry("targetMax", user.getTargetMax()),
                Map.entry("series", glucoseByDay.entrySet().stream().map(e -> Map.of("date", e.getKey(), "average", round(avg(e.getValue())), "count", e.getValue().size())).toList())));
        data.put("medication", Map.of("hasData", !medications.isEmpty(), "scheduled", medications.size(), "taken", taken, "missed", missed, "skipped", skipped, "onTime", onTime, "adherence", adherence));
        data.put("meals", Map.of("completed", meals.size(), "daysRecorded", mealDays, "totalCarbohydrate", round(totalCarbohydrate), "averageCarbohydrate", mealsWithCarbohydrate == 0 ? 0 : round(totalCarbohydrate / mealsWithCarbohydrate)));
        data.put("exercise", Map.of("minutes", exerciseMinutes, "days", exerciseDays, "sessions", exercises.size(), "averageMinutes", exercises.isEmpty() ? 0 : round(exerciseMinutes * 1.0 / exercises.size())));
        data.put("highlights", reportHighlights(glucose, normalCount, highCount, lowCount, glucoseDays, medications, adherence, meals, exerciseMinutes, exerciseDays));
        return data;
    }

    private boolean inTarget(UserAccount user, GlucoseRecord record) {
        return record.getValue().compareTo(user.getTargetMin()) >= 0 && record.getValue().compareTo(user.getTargetMax()) <= 0;
    }

    private boolean takenOnTime(MedicationRecord record) {
        if (!"taken".equals(record.getStatus()) || record.getTakenAt() == null || record.getScheduledAt() == null) return false;
        return Math.abs(Duration.between(record.getScheduledAt(), record.getTakenAt()).toMinutes()) <= 30;
    }

    private List<String> reportHighlights(List<GlucoseRecord> glucose, long normalCount, long highCount, long lowCount, long glucoseDays,
                                          List<MedicationRecord> medications, int adherence, List<MealRecord> meals, int exerciseMinutes, long exerciseDays) {
        List<String> highlights = new ArrayList<>();
        if (glucose.isEmpty()) {
            highlights.add("本期暂无血糖记录，完成测量后即可生成范围内比例和趋势分析。");
        } else {
            highlights.add("本期记录 " + glucose.size() + " 次血糖，覆盖 " + glucoseDays + " 天，平均值为 " + round(avg(glucose)) + " mmol/L。");
            int timeInRange = (int) Math.round(normalCount * 100.0 / glucose.size());
            if (lowCount > 0) highlights.add("有 " + lowCount + " 次血糖低于目标范围，建议关注发生时段并及时复测。");
            if (highCount > 0) highlights.add("有 " + highCount + " 次血糖高于目标范围，可结合饮食和运动记录查找原因。");
            if (lowCount == 0 && highCount == 0) highlights.add("本期所有血糖记录均处于个人目标范围内。");
            else if (timeInRange >= 70) highlights.add("范围内比例为 " + timeInRange + "% ，整体控制较稳定，但仍需关注异常记录。");
        }
        if (!medications.isEmpty()) highlights.add("共记录 " + medications.size() + " 次用药计划，已服用依从率为 " + adherence + "% 。");
        if (!meals.isEmpty()) highlights.add("饮食记录覆盖 " + meals.stream().map(m -> m.getEatenAt().toLocalDate()).distinct().count() + " 天，可用于对照餐后血糖变化。");
        if (exerciseMinutes > 0) highlights.add("累计运动 " + exerciseMinutes + " 分钟，覆盖 " + exerciseDays + " 天。");
        if (highlights.size() == 1 && glucose.isEmpty()) highlights.add("继续记录饮食、用药和运动后，报告会自动补充行为分析。");
        return highlights.stream().limit(5).toList();
    }

    private Map<String, Object> glucose(GlucoseRecord r) { return Map.of("id", r.getId(), "value", r.getValue(), "unit", r.getUnit(), "period", r.getPeriod(), "measuredAt", r.getMeasuredAt(), "status", r.getStatus(), "note", r.getNote() == null ? "" : r.getNote()); }
    private int percentageInRange(UserAccount user, List<GlucoseRecord> records) { if (records.isEmpty()) return 0; return (int) Math.round(records.stream().filter(g -> "normal".equals(g.getStatus())).count() * 100.0 / records.size()); }
    private double variability(List<GlucoseRecord> records) { if (records.size() < 2) return 0; double avg = avg(records); double variance = records.stream().mapToDouble(r -> Math.pow(r.getValue().doubleValue() - avg, 2)).average().orElse(0); return round(Math.sqrt(variance)); }
    private double avg(List<GlucoseRecord> records) { return records.stream().mapToDouble(r -> r.getValue().doubleValue()).average().orElse(0); }
    private double round(double value) { return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue(); }
    private String disclaimer() { return "建议仅供日常管理参考，不能替代医生诊断，不要自行调整药量。"; }
}
