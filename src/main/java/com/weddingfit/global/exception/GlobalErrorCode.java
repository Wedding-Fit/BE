package com.weddingfit.global.exception;

public enum GlobalErrorCode implements ErrorCode {
    
    // 400 Bad Request
    INVALID_INPUT_VALUE(400, "G001", "유효하지 않은 입력값입니다."),
    MISSING_PARAMETER(400, "G002", "필수 파라미터가 누락되었습니다."),
    
    // 401 Unauthorized
    UNAUTHORIZED(401, "G003", "인증이 필요합니다."),
    INVALID_TOKEN(401, "G004", "유효하지 않은 토큰입니다."),
    
    // 403 Forbidden
    FORBIDDEN(403, "G005", "접근 권한이 없습니다."),
    
    // 404 Not Found
    NOT_FOUND(404, "G006", "요청한 리소스를 찾을 수 없습니다."),
    
    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(405, "G007", "지원하지 않는 HTTP 메소드입니다."),
    
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(500, "G008", "서버 내부 오류가 발생했습니다."),
    
    // 503 Service Unavailable
    SERVICE_UNAVAILABLE(503, "G009", "서비스를 사용할 수 없습니다.");
    
    private final int status;
    private final String code;
    private final String message;
    
    GlobalErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
    
    @Override
    public int getStatus() {
        return status;
    }
    
    @Override
    public String getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}