package com.weddingfit.global.response;

import com.weddingfit.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    
    private boolean success;
    private String message;
    private String code;
    private int status;
    private LocalDateTime timestamp;
    private List<FieldError> errors;
    
    private ErrorResponse(ErrorCode errorCode, List<FieldError> errors) {
        this.success = false;
        this.message = errorCode.getMessage();
        this.code = errorCode.getCode();
        this.status = errorCode.getStatus();
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
    
    private ErrorResponse(ErrorCode errorCode) {
        this.success = false;
        this.message = errorCode.getMessage();
        this.code = errorCode.getCode();
        this.status = errorCode.getStatus();
        this.timestamp = LocalDateTime.now();
        this.errors = null;
    }
    
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }
    
    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(errorCode, errors);
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getCode() {
        return code;
    }
    
    public int getStatus() {
        return status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public List<FieldError> getErrors() {
        return errors;
    }
    
    // 필드 에러 클래스 (validation 용)
    public static class FieldError {
        private String field;
        private String value;
        private String reason;
        
        public FieldError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }
        
        public String getField() {
            return field;
        }
        
        public String getValue() {
            return value;
        }
        
        public String getReason() {
            return reason;
        }
    }
}