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
@Schema(name = "JeonseLoanListResponse", description = "전세자금대출 목록 조회 최상위 응답 DTO")
public class JeonseLoanListResponse {

    @Schema(description = "응답 코드", example = "200")
    private int code;

    @Schema(description = "응답 메시지", example = "성공했습니다")
    private String message;

    @Schema(description = "전세자금대출 목록 및 페이지 정보")
    private JeonseLoanListDataResponse data;
}
