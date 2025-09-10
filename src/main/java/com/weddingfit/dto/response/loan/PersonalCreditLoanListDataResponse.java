package com.weddingfit.dto.response.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PersonalCreditLoanListDataResponse", description = "개인신용대출 목록 페이징 데이터")
public class PersonalCreditLoanListDataResponse {

    @Schema(description = "개인신용대출 리스트")
    private List<PersonalCreditLoanListItemResponse> personalCreditLoanList;

    @Schema(description = "현재 페이지 (0부터 시작)", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @Schema(description = "총 페이지 수", example = "5")
    private int totalPages;

    @Schema(description = "총 항목 수", example = "42")
    private long totalCount;
}
