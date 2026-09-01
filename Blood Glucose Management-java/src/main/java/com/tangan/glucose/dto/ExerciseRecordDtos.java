package com.tangan.glucose.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class ExerciseRecordDtos {
    private ExerciseRecordDtos() { }
    public record Request(@NotBlank @Size(max = 30) String exerciseType, @NotNull OffsetDateTime startedAt,
                          @NotNull @Min(1) @Max(600) Integer durationMinutes,
                          @NotBlank @Pattern(regexp = "light|moderate|vigorous") String intensity,
                          @DecimalMin("0.0") @DecimalMax("40.0") BigDecimal beforeGlucose,
                          @DecimalMin("0.0") @DecimalMax("40.0") BigDecimal afterGlucose, @Size(max = 500) String note) { }
    public record Update(@Size(max = 30) String exerciseType, OffsetDateTime startedAt,
                         @Min(1) @Max(600) Integer durationMinutes,
                         @Pattern(regexp = "light|moderate|vigorous") String intensity,
                         @DecimalMin("0.0") @DecimalMax("40.0") BigDecimal beforeGlucose,
                         @DecimalMin("0.0") @DecimalMax("40.0") BigDecimal afterGlucose, @Size(max = 500) String note) { }
    public record Response(UUID id, String exerciseType, OffsetDateTime startedAt, Integer durationMinutes,
                           String intensity, BigDecimal beforeGlucose, BigDecimal afterGlucose, String note,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
}
