package com.weddingfit.controller;

import com.weddingfit.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "테스트", description = "서버 상태 확인 및 예외 처리 테스트 API")
public class TestController {
    
    @GetMapping("/")
    @Operation(summary = "서버 상태 확인", description = "API 서버가 정상 작동하는지 확인합니다.")
    public BaseResponse<String> home() {
        return BaseResponse.success("WeddingFit API Server is running!");
    }
    
    @GetMapping("/test")
    @Operation(summary = "테스트 엔드포인트", description = "기본 테스트용 엔드포인트입니다.")
    public BaseResponse<String> test() {
        return BaseResponse.success("Test endpoint works!");
    }
    
    // 예외 테스트용 엔드포인트들
    @GetMapping("/error/500")
    @Operation(summary = "500 에러 테스트", description = "서버 내부 오류 응답을 테스트합니다.")
    public BaseResponse<String> internalServerError() {
        throw new RuntimeException("테스트용 500 에러");
    }
    
    @GetMapping("/error/custom")
    @Operation(summary = "커스텀 에러 테스트", description = "IllegalArgumentException 처리를 테스트합니다.")
    public BaseResponse<String> customError() {
        if (true) { // 항상 에러 발생
            throw new IllegalArgumentException("커스텀 에러 테스트");
        }
        return BaseResponse.success("실행되지 않음");
    }
}