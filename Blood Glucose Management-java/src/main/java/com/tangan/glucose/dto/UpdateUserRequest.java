package com.tangan.glucose.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateUserRequest(@Size(max = 60) String name, @Size(max = 20) String diabetesType,
                               @Valid TargetRange targetRange, @Valid Doctor doctor, @Size(max = 50) String timezone) {
    public record TargetRange(@NotNull @DecimalMin("1.0") @DecimalMax("40.0") BigDecimal min,
                              @NotNull @DecimalMin("1.0") @DecimalMax("40.0") BigDecimal max) { }
    public record Doctor(@Size(max = 60) String name, @Size(max = 120) String clinic) { }
}
