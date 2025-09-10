package com.weddingfit.controller.loan;

import com.weddingfit.dto.response.loan.PersonalCreditLoanDetailItemResponse;
import com.weddingfit.dto.response.loan.PersonalCreditLoanListDataResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.loan.PersonalCreditLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-credit-loans")
@Tag(name = "Personal Credit Loan", description = "개인 신용 대출 상품 조회 API")
public class PersonalCreditLoanController {

    private final PersonalCreditLoanService service;

    @GetMapping
    @Operation(summary = "개인 신용 대출 목록 조회", description = "정렬/페이지 정보를 입력받아 개인 신용 대출 상품 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    public BaseResponse<PersonalCreditLoanListDataResponse> list(
            @RequestParam(required = false) String sort,  // "crdtGradAvg,DESC"(default)
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PersonalCreditLoanListDataResponse data = service.getList(sort, page, size);
        return BaseResponse.success(data, "성공했습니다");
    }

    @GetMapping("/{personalCreditLoanId}")
    @Operation(summary = "개인 신용 대출 상세 조회", description = "개인 신용 대출 상품의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상세 조회 성공")
    public BaseResponse<PersonalCreditLoanDetailItemResponse> detail(
            @PathVariable("personalCreditLoanId") Long id
    ) {
        PersonalCreditLoanDetailItemResponse item = service.getDetail(id);
        return BaseResponse.success(item, "성공했습니다");
    }
}
