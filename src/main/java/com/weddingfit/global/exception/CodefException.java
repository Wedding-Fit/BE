package com.weddingfit.global.exception;

public class CodefException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public CodefException(String errorCode, String errorMessage) {
        super(String.format("[%s] %s", errorCode, errorMessage));
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public CodefException(String errorCode, String errorMessage, Throwable cause) {
        super(String.format("[%s] %s", errorCode, errorMessage), cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}