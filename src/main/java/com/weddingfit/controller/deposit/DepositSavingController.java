package com.weddingfit.controller.deposit;

import com.weddingfit.dto.response.deposit.DepositSavingListDataResponse;
import com.weddingfit.dto.response.deposit.DepositSavingListResponse;
import com.weddingfit.service.deposit.DepositSavingService;
import com.weddingfit.dto.response.deposit.DepositSavingDetailItemResponse;
import com.weddingfit.dto.response.deposit.DepositSavingDetailResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposit-savings")
@Tag(name = "DepositSavings", description = "예금 & 적금 상품 조회 API")
public class DepositSavingController {

    private final DepositSavingService queryService;

   @GetMapping("/deposit")
    public ResponseEntity<DepositSavingListResponse> getDeposits(
            @RequestParam(required = false) String sort,        // "maxInterestRate,DESC"(default) | "interestRate,DESC"
            @RequestParam(defaultValue = "0") int page,         // 기본 0
            @RequestParam(defaultValue = "10") int size         // 기본 10
    ) {
        try {
            DepositSavingListDataResponse data = queryService.getDepositList(sort, page, size);
            return ResponseEntity.ok(
                    DepositSavingListResponse.builder()
                            .code(200)
                            .message("성공했습니다")
                            .data(data)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    DepositSavingListResponse.builder()
                            .code(401)
                            .message("실패했습니다")
                            .data(null)
                            .build()
            );
        }
    }

    @GetMapping("/deposit/{depositSavingId}")
    public ResponseEntity<DepositSavingDetailResponse> getDepositDetail(
            @PathVariable Long depositSavingId
    ) {
        try {
            DepositSavingDetailItemResponse item = queryService.getDepositDetail(depositSavingId);
            return ResponseEntity.ok(
                    DepositSavingDetailResponse.builder()
                            .code(200)
                            .message("성공했습니다")
                            .data(item)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    DepositSavingDetailResponse.builder()
                            .code(401)
                            .message("실패했습니다")
                            .data(null)
                            .build()
            );
        }
    }

    @GetMapping("/saving")
    public ResponseEntity<DepositSavingListResponse> getSavings(
            @RequestParam(required = false) String sort, // "maxInterestRate,DESC"(default) | "interestRate,DESC"
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            DepositSavingListDataResponse data = queryService.getSavingList(sort, page, size);
            return ResponseEntity.ok(
                    DepositSavingListResponse.builder()
                            .code(200).message("성공했습니다").data(data).build()
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    DepositSavingListResponse.builder()
                            .code(401).message("실패했습니다").data(null).build()
            );
        }
    }

    @GetMapping("/saving/{depositSavingId}")
    public ResponseEntity<DepositSavingDetailResponse> getSavingDetail(@PathVariable Long depositSavingId) {
        try {
            DepositSavingDetailItemResponse item = queryService.getSavingDetail(depositSavingId);
            return ResponseEntity.ok(
                    DepositSavingDetailResponse.builder()
                            .code(200).message("성공했습니다").data(item).build()
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    DepositSavingDetailResponse.builder()
                            .code(401).message("실패했습니다").data(null).build()
            );
        }
    }
}