package com.weddingfit.dto.response.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "JeonseLoanDetailItemResponse", description = "전세자금대출 상세 응답 DTO")
public class JeonseLoanDetailItemResponse {

    @Schema(description = "전세자금대출 ID", example = "123")
    private Long jeonseLoanId;

    @Schema(description = "금융회사 ID", example = "16")
    private Long financialCompanyId;

    @Schema(description = "금융회사명", example = "국민은행")
    private String financialCompanyName;

    @Schema(description = "상품명", example = "KB주택전세자금대출")
    private String name;

    @Schema(description = "중도상환해약금(수수료) 안내/산식 원문",
            example = "중도상환금액 × 0.59% × (대출잔여일수 ÷ 3년)")
    private String erlyFee;

    @Schema(description = "연체이자율 안내 원문",
            example = "대출금리+3.0% (최고 15%)")
    private String dlyRate;

    @Schema(description = "대출한도 안내 원문",
            example = "최대 5억원")
    private String loanLmt;

    @Schema(description = "최저 금리(연, %)", example = "3.65")
    private BigDecimal lendRateMin;

    @Schema(description = "최고 금리(연, %)", example = "5.05")
    private BigDecimal lendRateMax;

    @Schema(description = "평균 금리(연, %)", example = "4.12")
    private BigDecimal lendRateAvg;

    @Schema(description = "문의처(전화번호 등)", example = "1588-9999")
    private String contactNumber;

    @Schema(description = "상품 상세 페이지 URL",
            example = "https://bank.example.com/products/kb-lease-1")
    private String productUrl;

    @Schema(description = "원문 JSON 등 추가 정보(로그/디버깅 용)", example = "{\"dcls_month\":\"202508\"}")
    private String extraInfo;
}
