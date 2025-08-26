package com.weddingfit.controller;

import com.weddingfit.global.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/")
    public BaseResponse<String> home() {
        return BaseResponse.success("WeddingFit API Server is running!");
    }
    
    @GetMapping("/test")
    public BaseResponse<String> test() {
        return BaseResponse.success("Test endpoint works!");
    }
    
    // 예외 테스트용 엔드포인트들
    @GetMapping("/error/500")
    public BaseResponse<String> internalServerError() {
        throw new RuntimeException("테스트용 500 에러");
    }
    
    @GetMapping("/error/custom")
    public BaseResponse<String> customError() {
        if (true) { // 항상 에러 발생
            throw new IllegalArgumentException("커스텀 에러 테스트");
        }
        return BaseResponse.success("실행되지 않음");
    }
}