package com.tangan.glucose.common;

import java.time.*;

public record DateRange(OffsetDateTime from, OffsetDateTime to) {
    public static DateRange of(LocalDate from, LocalDate to) {
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        LocalDate start = from == null ? LocalDate.now(zone).minusDays(6) : from;
        LocalDate end = to == null ? LocalDate.now(zone) : to;
        if (end.isBefore(start)) throw ApiException.badRequest("时间范围无效");
        return new DateRange(start.atStartOfDay(zone).toOffsetDateTime(), end.plusDays(1).atStartOfDay(zone).toOffsetDateTime().minusNanos(1));
    }
}
