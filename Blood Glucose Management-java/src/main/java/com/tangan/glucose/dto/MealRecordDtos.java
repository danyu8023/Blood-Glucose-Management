package com.tangan.glucose.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public final class MealRecordDtos {
    private MealRecordDtos() { }
    public record Food(@NotBlank @Size(max = 80) String name, @NotNull @DecimalMin("0.01") BigDecimal amount, @NotBlank @Size(max = 20) String unit) { }
    public record Request(@NotBlank @Pattern(regexp = "breakfast|lunch|dinner|snack") String mealType,
                          @NotNull OffsetDateTime eatenAt, @NotEmpty List<@Valid Food> foods,
                          @DecimalMin("0.0") @DecimalMax("1000.0") BigDecimal carbohydrateGrams, @Size(max = 500) String note) { }
    public record Update(@Pattern(regexp = "breakfast|lunch|dinner|snack") String mealType,
                         OffsetDateTime eatenAt, List<@Valid Food> foods,
                         @DecimalMin("0.0") @DecimalMax("1000.0") BigDecimal carbohydrateGrams, @Size(max = 500) String note) { }
    public record Response(UUID id, String mealType, OffsetDateTime eatenAt, List<Food> foods,
                           BigDecimal carbohydrateGrams, String note, UUID linkedGlucoseRecordId,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
}
