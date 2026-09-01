package com.tangan.glucose.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class MedicationRecordDtos {
    private MedicationRecordDtos() { }
    public record Request(@NotBlank @Size(max = 120) String medicationName,
                          @NotNull @DecimalMin("0.01") @DecimalMax("100000") BigDecimal dose,
                          @NotBlank @Size(max = 20) String doseUnit, OffsetDateTime takenAt, OffsetDateTime scheduledAt,
                          @NotBlank @Pattern(regexp = "taken|missed|skipped") String status, @Size(max = 500) String note) { }
    public record Update(@Size(max = 120) String medicationName, @DecimalMin("0.01") @DecimalMax("100000") BigDecimal dose,
                         @Size(max = 20) String doseUnit, OffsetDateTime takenAt, OffsetDateTime scheduledAt,
                         @Pattern(regexp = "taken|missed|skipped") String status, @Size(max = 500) String note) { }
    public record Response(UUID id, String medicationName, BigDecimal dose, String doseUnit, OffsetDateTime takenAt,
                           OffsetDateTime scheduledAt, String status, String adherence, String note,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
}
