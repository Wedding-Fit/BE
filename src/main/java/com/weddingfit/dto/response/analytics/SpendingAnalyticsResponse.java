package com.weddingfit.dto.response.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SpendingAnalyticsResponse", description = "사용자의 지출 분석 응답 DTO")
public class SpendingAnalyticsResponse {

    @JsonProperty("totalCost")
    @Schema(description = "총 지출 금액", example = "1250000")
    private Long totalCost;

    @JsonProperty("aiMessage")
    @Schema(description = "AI 분석 메시지 (소비 인사이트/추천사항)",
            example = "이번 달 식비가 평균보다 20% 높습니다. 외식 대신 배달비 절약을 고려해 보세요.")
    private String aiMessage;

    @JsonProperty("food")
    @Schema(description = "식비 지출 분석")
    private CategorySpending food;

    @JsonProperty("culture")
    @Schema(description = "문화/여가 지출 분석")
    private CategorySpending culture;

    @JsonProperty("medical")
    @Schema(description = "의료/건강 지출 분석")
    private CategorySpending medical;

    @JsonProperty("transport")
    @Schema(description = "교통 지출 분석")
    private CategorySpending transport;

    @JsonProperty("shopping")
    @Schema(description = "쇼핑 지출 분석")
    private CategorySpending shopping;

    @JsonProperty("education")
    @Schema(description = "교육 지출 분석")
    private CategorySpending education;

    @JsonProperty("communication")
    @Schema(description = "통신 지출 분석")
    private CategorySpending communication;

    @JsonProperty("etc")
    @Schema(description = "기타 지출 분석")
    private CategorySpending etc;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "CategorySpending", description = "카테고리별 지출 분석 정보")
    public static class CategorySpending {

        @JsonProperty("cost")
        @Schema(description = "카테고리별 지출 금액", example = "350000")
        private Long cost;

        @JsonProperty("average")
        @Schema(description = "평균 지출 금액 (비교 기준)", example = "280000")
        private Long average;
    }
}