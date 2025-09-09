package com.weddingfit.controller.analytics;

import com.weddingfit.dto.response.analytics.SpendingAnalyticsResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.global.security.JwtAuthenticationUtil;
import com.weddingfit.service.analytics.SpendingAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Spending Analytics", description = "소비 패턴 분석 API")
public class SpendingAnalyticsController {
    
    private final SpendingAnalyticsService spendingAnalyticsService;
    private final JwtAuthenticationUtil jwtAuthenticationUtil;
    
    @GetMapping("/spending-analytics")
    @Operation(
            summary = "소비 패턴 분석 조회",
            description = "사용자의 소비 패턴을 분석하여 카테고리별 지출 내역과 AI 추천 메시지를 제공합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "소비 패턴 분석 조회 성공",
                    content = @Content(schema = @Schema(implementation = SpendingAnalyticsResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
            @ApiResponse(responseCode = "404", description = "분석할 거래 데이터가 없습니다")
    })
    public BaseResponse<SpendingAnalyticsResponse> getSpendingAnalytics(HttpServletRequest httpRequest) {
        Long userId = jwtAuthenticationUtil.getCurrentUserId(httpRequest);
        
        try {
            SpendingAnalyticsResponse response = spendingAnalyticsService.getSpendingAnalytics(userId);
            return BaseResponse.success(response, "소비패턴 분석 조회에 성공했습니다");
        } catch (IllegalArgumentException e) {
            return BaseResponse.error(404, "분석할 거래 데이터가 없습니다");
        }
    }
}