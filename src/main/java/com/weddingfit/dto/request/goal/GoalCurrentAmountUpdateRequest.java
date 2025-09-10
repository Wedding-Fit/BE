package com.weddingfit.dto.request.goal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "목표 현재 금액 업데이트 요청")
public class GoalCurrentAmountUpdateRequest {

    @NotNull(message = "현재 금액은 필수값입니다")
    @PositiveOrZero(message = "현재 금액은 0 이상이어야 합니다")
    @Schema(description = "현재 적립 금액", example = "1500000")
    private BigDecimal currentAmount;

    @Schema(description = "업데이트 사유", example = "적금 입금 완료")
    private String reason;
}