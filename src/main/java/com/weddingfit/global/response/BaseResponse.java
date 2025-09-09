package com.weddingfit.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    
    private int code;
    private T data;
    private String message;
    private String errorCode;
    
    private BaseResponse(int code, T data, String message, String errorCode) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    // 성공 응답
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, data, "성공했습니다", null);
    }
    
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(200, data, message, null);
    }
    
    // 실패 응답
    public static <T> BaseResponse<T> error(String message, String errorCode) {
        return new BaseResponse<>(401, null, message, errorCode);
    }
    
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message, null);
    }
    
    // Getters
    public int getCode() {
        return code;
    }
    
    public T getData() {
        return data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}