package com.tangan.glucose.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class GlucoseRecordDtos {
    private GlucoseRecordDtos() { }
    public record Request(@NotNull @DecimalMin("1.0") @DecimalMax("40.0") BigDecimal value,
                          @Pattern(regexp = "mmol/L") String unit,
                          @NotBlank @Pattern(regexp = "fasting|pre_meal|post_meal|bedtime") String period,
                          @NotNull OffsetDateTime measuredAt, @Size(max = 500) String note) { }
    public record Update(@DecimalMin("1.0") @DecimalMax("40.0") BigDecimal value,
                         @Pattern(regexp = "mmol/L") String unit,
                         @Pattern(regexp = "fasting|pre_meal|post_meal|bedtime") String period,
                         OffsetDateTime measuredAt, @Size(max = 500) String note) { }
    public record Response(UUID id, BigDecimal value, String unit, String period, OffsetDateTime measuredAt,
                           String status, String note, OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
}
