package com.weddingfit.dto.response.deposit;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "DepositSavingListDataResponse",
        description = "예금/적금 상품 목록 페이징 응답의 데이터 본문")
public class DepositSavingListDataResponse {

    @ArraySchema(
        arraySchema = @Schema(description = "예금/적금 상품 리스트"),
        schema = @Schema(implementation = DepositSavingListItemResponse.class)
    )
    private List<DepositSavingListItemResponse> depositSavingList;

    @Schema(description = "현재 페이지(0부터 시작)", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "전체 페이지 수", example = "3")
    private int totalPages;

    @Schema(description = "전체 데이터 건수", example = "27")
    private long totalCount;
}
