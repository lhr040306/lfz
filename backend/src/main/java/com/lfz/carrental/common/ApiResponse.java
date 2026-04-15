package com.lfz.carrental.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(0, message, null);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(-1, message, null);
    }
}

