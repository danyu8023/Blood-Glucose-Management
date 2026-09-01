package com.tangan.glucose.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(@NotBlank String account, @NotBlank @Size(min = 6, max = 100) String password,
                           @Size(max = 120) String deviceName) { }
