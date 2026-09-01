package com.tangan.glucose.common;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final int status;
    private final int code;

    public ApiException(int status, int code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException badRequest(String message) { return new ApiException(400, 40000, message); }
    public static ApiException unauthorized(String message) { return new ApiException(401, 40100, message); }
    public static ApiException forbidden(String message) { return new ApiException(403, 40300, message); }
    public static ApiException notFound(String message) { return new ApiException(404, 40400, message); }
    public static ApiException conflict(String message) { return new ApiException(409, 40900, message); }
}
