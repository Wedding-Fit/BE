package com.weddingfit.dto.response.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "JeonseLoanListDataResponse", description = "전세자금대출 목록 페이징 응답 데이터")
public class JeonseLoanListDataResponse {

    @Schema(
        description = "전세자금대출 목록",
        implementation = JeonseLoanListItemResponse.class
    )
    private List<JeonseLoanListItemResponse> jeonseLoanList;

    @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
    private int page;

    @Schema(description = "페이지 당 항목 수", example = "10")
    private int size;

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;

    @Schema(description = "전체 항목 수", example = "42")
    private long totalCount;
}
