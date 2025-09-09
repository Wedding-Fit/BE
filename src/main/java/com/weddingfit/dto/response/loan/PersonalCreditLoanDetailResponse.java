package com.weddingfit.dto.response.loan;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "PersonalCreditLoanDetailResponse", description = "개인신용대출 상세 응답 래퍼")
public class PersonalCreditLoanDetailResponse {

    @Schema(description = "응답 코드 (성공 200, 실패 401 등)", example = "200")
    private int code;

    @Schema(description = "응답 메시지", example = "성공했습니다")
    private String message;

    @Schema(description = "개인신용대출 상세 데이터")
    private PersonalCreditLoanDetailItemResponse data;
}
