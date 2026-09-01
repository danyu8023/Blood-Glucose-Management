package com.tangan.glucose.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class RecommendationDtos {
    private RecommendationDtos() { }
    public record Request(@NotNull @DecimalMin("0.0") @DecimalMax("40.0") BigDecimal glucoseValue,
                          @NotBlank @Pattern(regexp = "fasting|pre_meal|post_meal|bedtime") String period,
                          UUID recordId) { }
    public record Response(String risk, String title, String summary, String diet, String medication,
                           List<String> actions, boolean urgent, String disclaimer) { }
}
