package com.weddingfit.dto.response.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "JeonseLoanDetailResponse", description = "전세자금대출 상세 API의 최상위 응답")
public class JeonseLoanDetailResponse {

    @Schema(description = "응답 코드", example = "200")
    private int code;

    @Schema(description = "응답 메시지", example = "성공했습니다")
    private String message;

    @Schema(description = "전세자금대출 상세 데이터")
    private JeonseLoanDetailItemResponse data;
}
