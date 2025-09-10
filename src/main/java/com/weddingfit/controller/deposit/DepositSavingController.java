package com.weddingfit.controller.deposit;

import com.weddingfit.dto.response.deposit.DepositSavingListDataResponse;
import com.weddingfit.dto.response.deposit.DepositSavingDetailItemResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.deposit.DepositSavingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposit-savings")
@Tag(name = "DepositSavings", description = "예금 & 적금 상품 조회 API")
public class DepositSavingController {

    private final DepositSavingService queryService;

    @GetMapping("/deposit")
    @Operation(summary = "예금 상품 목록 조회", description = "정렬/페이지 정보를 입력받아 예금 상품 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "예금 목록 조회 성공")
    public BaseResponse<DepositSavingListDataResponse> getDeposits(
            @RequestParam(required = false) String sort,        // "maxInterestRate,DESC"(default) | "interestRate,DESC"
            @RequestParam(defaultValue = "0") int page,         // 기본 0
            @RequestParam(defaultValue = "10") int size         // 기본 10
    ) {
        DepositSavingListDataResponse data = queryService.getDepositList(sort, page, size);
        return BaseResponse.success(data, "성공했습니다");
    }

    @GetMapping("/deposit/{depositSavingId}")
    @Operation(summary = "예금 상품 상세 조회", description = "예금 상품의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "예금 상세 조회 성공")
    public BaseResponse<DepositSavingDetailItemResponse> getDepositDetail(
            @PathVariable Long depositSavingId
    ) {
        DepositSavingDetailItemResponse item = queryService.getDepositDetail(depositSavingId);
        return BaseResponse.success(item, "성공했습니다");
    }

    @GetMapping("/saving")
    @Operation(summary = "적금 상품 목록 조회", description = "정렬/페이지 정보를 입력받아 적금 상품 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "적금 목록 조회 성공")
    public BaseResponse<DepositSavingListDataResponse> getSavings(
            @RequestParam(required = false) String sort, // "maxInterestRate,DESC"(default) | "interestRate,DESC"
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        DepositSavingListDataResponse data = queryService.getSavingList(sort, page, size);
        return BaseResponse.success(data, "성공했습니다");
    }

    @GetMapping("/saving/{depositSavingId}")
    @Operation(summary = "적금 상품 상세 조회", description = "적금 상품의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "적금 상세 조회 성공")
    public BaseResponse<DepositSavingDetailItemResponse> getSavingDetail(
            @PathVariable Long depositSavingId
    ) {
        DepositSavingDetailItemResponse item = queryService.getSavingDetail(depositSavingId);
        return BaseResponse.success(item, "성공했습니다");
    }
}
