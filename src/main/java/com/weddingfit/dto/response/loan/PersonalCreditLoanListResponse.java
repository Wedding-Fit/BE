package com.weddingfit.dto.response.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PersonalCreditLoanListResponse", description = "개인신용대출 목록 조회 응답 래퍼")
public class PersonalCreditLoanListResponse {

    @Schema(description = "응답 코드", example = "200")
    private int code;

    @Schema(description = "응답 메시지", example = "성공했습니다")
    private String message;

    @Schema(description = "목록 데이터")
    private PersonalCreditLoanListDataResponse data;
}
