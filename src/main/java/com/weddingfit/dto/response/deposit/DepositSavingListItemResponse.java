package com.weddingfit.dto.response.deposit;

import com.weddingfit.entity.deposit.DepositSavingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "DepositSavingResponse", description = "예·적금 상품 조회 응답 DTO")
public class DepositSavingListItemResponse {
    @Schema(description = "예·적금 상품 ID", example = "1")
    private Long depositSavingId;

    @Schema(description = "금융회사 ID", example = "101")
    private Long financialCompanyId;

    @Schema(description = "금융회사명", example = "하나은행")
    private String financialCompanyName;

    @Schema(description = "상품 유형", example = "DEPOSIT", allowableValues = {"DEPOSIT", "SAVING"})
    private DepositSavingType type; // DEPOSIT | SAVING

    @Schema(description = "상품명", example = "하나 원가예금")
    private String name;

    @Schema(description = "기본 금리(연)", example = "3.95")
    private BigDecimal interestRate;

    @Schema(description = "최대 우대금리 포함 금리(연)", example = "4.50")
    private BigDecimal maxInterestRate;
}
