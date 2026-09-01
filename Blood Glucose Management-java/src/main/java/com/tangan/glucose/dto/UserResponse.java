package com.tangan.glucose.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponse(UUID id, String name, String account, String diabetesType, String phone,
                           TargetRange targetRange, Doctor doctor, String timezone) {
    public record TargetRange(BigDecimal min, BigDecimal max, String unit) { }
    public record Doctor(String name, String clinic) { }
}
