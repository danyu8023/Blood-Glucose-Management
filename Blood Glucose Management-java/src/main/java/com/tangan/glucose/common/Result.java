package com.tangan.glucose.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 统一 API 响应，Controller 不直接暴露 Entity。 */
@Getter
@AllArgsConstructor
public class Result<T> {
    private final int code;
    private final String message;
    private final T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "ok", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    public static <T> Result<T> failure(int code, String message) {
        return new Result<>(code, message, null);
    }
}
