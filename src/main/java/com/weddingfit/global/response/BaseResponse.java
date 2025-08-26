package com.weddingfit.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    
    private BaseResponse(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }
    
    // 성공 응답
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "요청이 성공했습니다.", data, null);
    }
    
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(true, message, data, null);
    }
    
    // 실패 응답
    public static <T> BaseResponse<T> error(String message, String errorCode) {
        return new BaseResponse<>(false, message, null, errorCode);
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public T getData() {
        return data;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}