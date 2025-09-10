package com.weddingfit.dto.response.couple;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoupleSummaryResponse {
    @Schema(
        description = "결혼식 날짜까지의 일수. 미래는 음수, 당일은 0, 지나면 양수",
        example = "-9"
    )
    private int dDay;

    @Schema(description = "남성 이름", example = "보성")
    private String maleName;

    @Schema(description = "여성 이름", example = "예빈")
    private String femaleName;

    @Schema(description = "커플 총 금액(예: 자산/예산 합계)", example = "35000000")
    private BigDecimal totalAmount;
}
