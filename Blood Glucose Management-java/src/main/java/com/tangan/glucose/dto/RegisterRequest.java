package com.tangan.glucose.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 60) String name,
        @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的 11 位手机号") String phone,
        @NotBlank @Size(min = 6, max = 100) String password,
        @AssertTrue(message = "必须同意用户协议和隐私政策") boolean consent,
        @Size(max = 50) String timezone
) { }
