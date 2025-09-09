package com.weddingfit.global.exception;

import com.weddingfit.global.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.DateTimeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 404 - 리소스를 찾을 수 없음
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<BaseResponse<Object>> handleNotFoundException(Exception e) {
        BaseResponse<Object> response = BaseResponse.error(404, "요청한 리소스를 찾을 수 없습니다");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    // 405 - 지원하지 않는 HTTP 메소드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Object>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        BaseResponse<Object> response = BaseResponse.error(405, "지원하지 않는 HTTP 메소드입니다");
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    // 400 - Validation 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("잘못된 입력값입니다");
        
        BaseResponse<Object> response = BaseResponse.error(400, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    // 500 - 내부 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleException(Exception e) {
        log.error("서버 내부 오류 발생: {}", e.getMessage(), e);
        BaseResponse<Object> response = BaseResponse.error(500, "서버 내부 오류가 발생했습니다");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // 사용자 정의 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException e) {
        BaseResponse<Object> response = BaseResponse.error(e.getErrorCode().getStatus(), e.getErrorCode().getMessage());
        return new ResponseEntity<>(response, HttpStatus.valueOf(e.getErrorCode().getStatus()));
    }

    // 지정한 포맷에 맞지 않는 입력이 들어왔을 때 발생
    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<BaseResponse<Object>> handleDateTimeParseException(DateTimeException e){
        BaseResponse<Object> response = BaseResponse.error(400, "날짜 형식이 올바르지 않습니다");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}