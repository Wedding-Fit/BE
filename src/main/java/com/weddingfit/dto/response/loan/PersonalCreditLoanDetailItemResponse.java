package com.weddingfit.dto.response.loan;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "PersonalCreditLoanDetailItemResponse", description = "개인신용대출 상세 응답 아이템 DTO")
public class PersonalCreditLoanDetailItemResponse {

    @Schema(description = "개인신용대출 ID", example = "15")
    private Long personalCreditLoanId;

    @Schema(description = "금융회사 ID", example = "101")
    private Long financialCompanyId;

    @Schema(description = "금융회사명", example = "국민은행")
    private String financialCompanyName;

    @Schema(description = "상품명", example = "KB 플러스 신용대출")
    private String name;

    @Schema(description = "가입 경로", example = "영업점,모집인")
    private String joinWay;

    @Schema(description = "신용등급 평균금리(가공값 포함)", example = "3.85")
    private BigDecimal crdtGradAvg;

    @Schema(description = "문의처", example = "1588-0000")
    private String contactNumber;

    @Schema(description = "상품 상세 URL", example = "https://bank.example.com/loan/kb-plus")
    private String productUrl;

    @Schema(description = "원문 JSON 등 추가 정보", example = "{\"raw\":\"...\"}")
    private String extraInfo;
}
