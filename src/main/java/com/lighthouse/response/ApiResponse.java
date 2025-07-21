package com.lighthouse.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final int code;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공 응답
    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(true, successCode.getCode(), successCode.getMessage(), data);
    }

    public static <T> ApiResponse<T> success(SuccessCode successCode) {
        return new ApiResponse<>(true, successCode.getCode(), successCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

}