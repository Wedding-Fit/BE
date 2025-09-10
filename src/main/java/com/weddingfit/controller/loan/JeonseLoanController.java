package com.weddingfit.controller.loan;

import com.weddingfit.dto.response.loan.JeonseLoanDetailItemResponse;
import com.weddingfit.dto.response.loan.JeonseLoanListDataResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.loan.JeonseLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jeonse-loans")
@Tag(name = "Jeonse Loan", description = "전세 대출 상품 조회 API")
public class JeonseLoanController {

    private final JeonseLoanService service;

    @GetMapping
    @Operation(summary = "전세 대출 목록 조회", description = "정렬/페이지 정보를 입력받아 전세 대출 상품 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    public BaseResponse<JeonseLoanListDataResponse> list(
            @RequestParam(required = false) String sort, // "lendRateAvg,DESC"(default) | "lendRateMin,ASC" ...
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        JeonseLoanListDataResponse data = service.getList(sort, page, size);
        return BaseResponse.success(data, "성공했습니다");
    }

    @GetMapping("/{jeonseLoanId}")
    @Operation(summary = "전세 대출 상세 조회", description = "전세 대출 상품의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상세 조회 성공")
    public BaseResponse<JeonseLoanDetailItemResponse> detail(
            @PathVariable("jeonseLoanId") Long id
    ) {
        JeonseLoanDetailItemResponse item = service.getDetail(id);
        return BaseResponse.success(item, "성공했습니다");
    }
}
