package com.weddingfit.dto.response.couple;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "커플 정보 조회 응답 DTO")
public class CoupleInfoResponse {

    @Schema(description = "파트너 로그인 ID", example = "partner123")
    private String loginId;

    @Schema(description = "결혼 지역", example = "서울특별시")
    private String region;

    @Schema(description = "웨딩 타입", example = "스몰 웨딩")
    private String weddingType;

    @Schema(description = "허니문 예산 포함 여부", example = "true")
    private Boolean honeymoonBudget;

    @Schema(description = "포토 패키지 포함 여부", example = "false")
    private Boolean photoPackage;

    @Schema(description = "드레스/메이크업 포함 여부", example = "true")
    private Boolean dressMakeup;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "결혼 예정일 (yyyy-MM-dd)", example = "2025-08-30")
    private LocalDate weddingDate;
}