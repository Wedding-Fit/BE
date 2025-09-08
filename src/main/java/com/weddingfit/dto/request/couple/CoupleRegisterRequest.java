package com.weddingfit.dto.request.couple;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoupleRegisterRequest {

    @NotBlank(message = "상대방 로그인 ID는 필수입니다")
    @Schema(description = "상대방 로그인 ID (user2)", example = "partner123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String loginId;

    @NotBlank(message = "지역은 필수입니다")
    @Schema(description = "결혼 지역", example = "서울특별시", requiredMode = Schema.RequiredMode.REQUIRED)
    private String region;

    @NotBlank(message = "웨딩 타입은 필수입니다")
    @Schema(description = "웨딩 타입", example = "스몰 웨딩", requiredMode = Schema.RequiredMode.REQUIRED)
    private String weddingType;

    @NotNull(message = "허니문 예산 여부는 필수입니다")
    @Schema(description = "허니문 예산 포함 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean honeymoonBudget;

    @NotNull(message = "포토 패키지 여부는 필수입니다")
    @Schema(description = "포토 패키지 포함 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean photoPackage;

    @NotNull(message = "드레스 메이크업 여부는 필수입니다")
    @Schema(description = "드레스/메이크업 포함 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean dressMakeup;

    @NotNull(message = "웨딩 날짜는 필수입니다")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "결혼 예정일 (yyyy-MM-dd)", example = "2025-08-30", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate weddingDate;
}