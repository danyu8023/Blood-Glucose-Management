package com.tangan.glucose.config;

import com.tangan.glucose.entity.*;
import com.tangan.glucose.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Slf4j @Configuration @RequiredArgsConstructor
public class DataInitializer {
    private final UserAccountRepository userRepository;
    private final UserSettingRepository settingRepository;
    private final GlucoseRecordRepository glucoseRepository;
    private final MealRecordRepository mealRepository;
    private final MedicationRecordRepository medicationRepository;
    private final ExerciseRecordRepository exerciseRepository;
    private final PublicContentRepository contentRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    @Bean
    CommandLineRunner seedData() { return args -> transactionTemplate.executeWithoutResult(status -> seed()); }

    @Transactional(rollbackFor = Exception.class)
    public void seed() {
        UserAccount user = userRepository.findByAccountAndActiveTrue("13800000000").orElseGet(() -> {
            UserAccount u = new UserAccount(); u.setName("张明"); u.setAccount("13800000000"); u.setPhone("13800000000"); u.setPasswordHash(passwordEncoder.encode("123456")); return userRepository.save(u);
        });
        if (!settingRepository.existsById(user.getId())) { UserSetting s = new UserSetting(); s.setUser(user); settingRepository.save(s); }
        if (glucoseRepository.countByUserIdAndDeletedFalseAndMeasuredAtBetween(user.getId(), OffsetDateTime.now().minusDays(7), OffsetDateTime.now().plusDays(1)) == 0) {
            addGlucose(user, new BigDecimal("6.8"), "post_meal", "燕麦粥、鸡蛋、无糖豆浆", 0);
            addGlucose(user, new BigDecimal("5.6"), "fasting", "", 0);
            addGlucose(user, new BigDecimal("7.2"), "post_meal", "糙米饭、清炒西兰花", 1);
        }
        seedPublic("post-meal-walk-15-minutes", "article", "news", "餐后步行 15 分钟", "轻松步行有助于平稳餐后血糖。", "餐后 20–30 分钟开始，持续 15–20 分钟。", "餐后步行 15 分钟有助于平稳餐后血糖。", "https://images.unsplash.com/photo-1551632811-561732d1e306?auto=format&fit=crop&w=900&q=80");
        seedPublic("balanced-plate-order", "article", "class", "一餐先吃蔬菜和蛋白质", "调整进餐顺序，帮助增加饱腹感并减缓餐后上升。", "先吃非淀粉蔬菜，再吃蛋白质，最后吃主食。", "建议餐盘分为 1/2 蔬菜、1/4 蛋白质、1/4 主食。", null);
        seedGuide("glucose-guide", "血糖怎么测", "固定时段记录，趋势才有可比性。", "空腹|起床后、进食前测量。\n餐前|进餐前立即测量并标记餐次。\n餐后 2 小时|从第一口饭开始计时。\n睡前|睡前测量，观察夜间风险。", "glucose");
        seedGuide("meals-guide", "记录饮食有什么用", "把血糖变化和具体食物对应起来。", "记录餐次与时间|食物名称、主食份量、甜饮和零食。\n连续记录|3–7 天更容易发现规律。", "meals");
        seedGuide("medication-guide", "用药记录要点", "记录依从性，复诊时更容易核对。", "记录内容|药物名称、剂量、服用时间。\n安全提醒|不要根据单次读数自行加药、减药或停药。", "medication");
        seedGuide("trend-guide", "趋势指标怎么看", "连续 7–30 天的变化更适合复盘管理效果。", "范围内时间|反映血糖处于目标范围的比例。\n平均值|观察整体水平，但不能代替高低血糖事件关注。", "trend");
        log.info("Demo data ensured for account 13800000000");
    }

    private void addGlucose(UserAccount user, BigDecimal value, String period, String note, int daysAgo) {
        GlucoseRecord g = new GlucoseRecord(); g.setUser(user); g.setValue(value); g.setUnit("mmol/L"); g.setPeriod(period); g.setMeasuredAt(OffsetDateTime.now().minusDays(daysAgo)); g.setStatus(value.compareTo(user.getTargetMax()) > 0 ? "high" : value.compareTo(user.getTargetMin()) < 0 ? "low" : "normal"); g.setNote(note); glucoseRepository.save(g);
    }
    private void seedPublic(String slug, String type, String category, String title, String summary, String lead, String body, String cover) {
        if (contentRepository.findBySlugAndPublishedTrue(slug).isPresent()) return;
        PublicContent c = new PublicContent(); c.setSlug(slug); c.setContentType(type); c.setCategory(category); c.setTitle(title); c.setSummary(summary); c.setLead(lead); c.setBody(body); c.setCoverUrl(cover); c.setPublishedAt(OffsetDateTime.now().minusDays(1)); contentRepository.save(c);
    }
    private void seedGuide(String slug, String title, String lead, String sections, String category) {
        if (contentRepository.findBySlugAndPublishedTrue(slug).isPresent()) return;
        StringBuilder json = new StringBuilder("[");
        String[] parts = sections.split("\\n"); for (int i = 0; i < parts.length; i++) { String[] p = parts[i].split("\\|", 2); if (i > 0) json.append(','); json.append("{\"heading\":\"").append(p[0]).append("\",\"body\":\"").append(p.length > 1 ? p[1] : "").append("\"}"); }
        json.append(']');
        PublicContent c = new PublicContent(); c.setSlug(slug); c.setContentType("guide"); c.setCategory(category); c.setTitle(title); c.setLead(lead); c.setBody(json.toString()); c.setPublishedAt(OffsetDateTime.now()); contentRepository.save(c);
    }
}
