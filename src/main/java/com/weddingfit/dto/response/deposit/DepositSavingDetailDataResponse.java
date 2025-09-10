package com.weddingfit.dto.response.deposit;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Schema(name = "DepositSavingDetailDataResponse",
        description = "예금/적금 상세 목록(또는 단건을 리스트 형태로 담는) 응답 페이로드")
public class DepositSavingDetailDataResponse {

    @ArraySchema(
        arraySchema = @Schema(description = "예금/적금 상세 항목 리스트 (단건 응답이면 size=1 권장)"),
        schema = @Schema(implementation = DepositSavingDetailItemResponse.class)
    )
    private List<DepositSavingDetailItemResponse> depositSavingList;

    @Schema(description = "페이지 번호 (상세는 0 고정 권장)", example = "0")
    private int page;

    @Schema(description = "응답에 포함된 항목 수", example = "1")
    private int size;

    @Schema(description = "전체 건수", example = "1")
    private long totalCount;
}
