package com.weddingfit.dto.response.deposit;

import com.weddingfit.entity.deposit.DepositSavingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Schema(name = "DepositSavingDetailItemResponse",
        description = "예금/적금 상품 상세 응답 항목")
public class DepositSavingDetailItemResponse {

    @Schema(description = "예금/적금 상품 ID", example = "1")
    private Long depositSavingId;

    @Schema(description = "금융회사 ID", example = "101")
    private Long financialCompanyId;

    @Schema(description = "금융회사명", example = "하나은행")
    private String financialCompanyName;

    @Schema(description = "상품 유형 (예금/적금)", example = "DEPOSIT",
            implementation = DepositSavingType.class)
    private DepositSavingType type;  // DEPOSIT | SAVING

    @Schema(description = "상품명", example = "하나 원가예금")
    private String name;

    @Schema(description = "기본 금리(%)", example = "4.21")
    private BigDecimal interestRate;

    @Schema(description = "최대 금리(%)", example = "4.80")
    private BigDecimal maxInterestRate;

    @Schema(description = "예치/적립 기간(월)", example = "12")
    private String saveMonth;       // "6", "12" ...

    @Schema(description = "유의사항/비고",
            example = "중도해지 시 약정이율 미적용. 예금자보호 5천만원 한도.")
    private String etcNote;         // 비고/유의사항

    @Schema(description = "문의처", example = "1588-1111")
    private String contactNumber;   // 문의처

    @Schema(description = "상품 상세 페이지 URL",
            example = "https://bank.example.com/products/hanabank-1")
    private String productUrl;      // 상품 페이지

    @Schema(description = "추가 정보(원문 JSON 등)", example = "{\"raw\":\"...\"}")
    private String extraInfo;       // 원문 JSON 등
}
