package com.weddingfit.dto.response.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PersonalCreditLoanListItemResponse", description = "개인신용대출 목록 아이템 DTO")
public class PersonalCreditLoanListItemResponse {

    @Schema(description = "개인신용대출 ID", example = "12")
    private Long personalCreditLoanId;

    @Schema(description = "금융회사 ID", example = "101")
    private Long financialCompanyId;

    @Schema(description = "금융회사명", example = "신한은행")
    private String financialCompanyName;

    @Schema(description = "상품명", example = "신한 마이신용대출")
    private String name;

    @Schema(description = "신용등급 평균 금리(가공값, %)", example = "3.75")
    private BigDecimal crdtGradAvg; // 신용등급 평균금리(가공값)
}
