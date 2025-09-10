package com.weddingfit.dto.response.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Schema(name = "DepositSavingDetailResponse",
        description = "예금/적금 단건 상세 API의 응답 래퍼")
public class DepositSavingDetailResponse {

    @Schema(description = "응답 코드", example = "200")
    private int code;

    @Schema(description = "응답 메시지", example = "성공했습니다")
    private String message;

    @Schema(description = "상세 데이터(예금/적금 상품 1건)",
            implementation = DepositSavingDetailItemResponse.class)
    private DepositSavingDetailItemResponse data;
}
