package com.weddingfit.controller.loan;

import com.weddingfit.dto.response.loan.*;
import com.weddingfit.service.loan.JeonseLoanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jeonse-loans")
@Tag(name = "Jeonse Loan", description = "전세 대출 상품 조회 API")
public class JeonseLoanController {
    private final JeonseLoanService service;

    @GetMapping
    public ResponseEntity<JeonseLoanListResponse> list(
            @RequestParam(required = false) String sort, // "lendRateAvg,DESC"(default) | "lendRateMin,ASC" ...
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            JeonseLoanListDataResponse data = service.getList(sort, page, size);
            return ResponseEntity.ok(
                    JeonseLoanListResponse.builder().code(200).message("성공했습니다").data(data).build()
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    JeonseLoanListResponse.builder().code(401).message("실패했습니다").data(null).build()
            );
        }
    }

    @GetMapping("/{jeonseLoanId}")
    public ResponseEntity<JeonseLoanDetailResponse> detail(@PathVariable("jeonseLoanId") Long id) {
        try {
            JeonseLoanDetailItemResponse item = service.getDetail(id);
            return ResponseEntity.ok(
                    JeonseLoanDetailResponse.builder().code(200).message("성공했습니다").data(item).build()
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    JeonseLoanDetailResponse.builder().code(401).message("실패했습니다").data(null).build()
            );
        }
    }
}
