package com.weddingfit.dto.response.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "JeonseLoanListItemResponse", description = "전세자금대출 목록의 개별 항목 응답 DTO")
public class JeonseLoanListItemResponse {

    @Schema(description = "전세대출 ID", example = "1")
    private Long jeonseLoanId;

    @Schema(description = "금융회사 ID", example = "101")
    private Long financialCompanyId;

    @Schema(description = "금융회사명", example = "하나은행")
    private String financialCompanyName;

    @Schema(description = "상품명", example = "KB주택전세자금대출")
    private String name;

    @Schema(description = "최저 금리(%)", example = "3.21")
    private BigDecimal lendRateMin;

    @Schema(description = "최고 금리(%)", example = "4.50")
    private BigDecimal lendRateMax;

    @Schema(description = "평균 금리(%)", example = "3.85")
    private BigDecimal lendRateAvg;
}
