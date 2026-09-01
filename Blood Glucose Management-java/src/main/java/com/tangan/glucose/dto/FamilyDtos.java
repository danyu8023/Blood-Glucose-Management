package com.tangan.glucose.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;

public final class FamilyDtos {
    private FamilyDtos() { }
    public record Request(@NotBlank @Size(max = 120) String contact, @NotBlank @Size(max = 30) String relationship,
                          @NotEmpty List<@Pattern(regexp = "critical_alerts|weekly_report") String> permissions) { }
    public record Response(UUID id, String contact, String relationship, List<String> permissions,
                           String status, OffsetDateTime expiresAt, OffsetDateTime createdAt) { }
}
