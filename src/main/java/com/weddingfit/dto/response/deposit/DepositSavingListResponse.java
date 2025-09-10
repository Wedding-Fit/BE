package com.weddingfit.dto.response.deposit;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "DepositSavingListResponse", description = "예금/적금 목록 조회 최상위 응답")
public class DepositSavingListResponse {

    @Schema(description = "응답 코드", example = "200")
    private int code;

    @Schema(description = "응답 메시지", example = "성공했습니다")
    private String message;

    @Schema(description = "목록 데이터 본문", implementation = DepositSavingListDataResponse.class)
    private DepositSavingListDataResponse data;
}

