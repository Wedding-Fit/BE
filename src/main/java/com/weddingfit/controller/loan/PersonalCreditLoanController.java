package com.weddingfit.controller.loan;

import com.weddingfit.dto.response.loan.*;
import com.weddingfit.service.loan.PersonalCreditLoanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-credit-loans")
@Tag(name = "Personal Credit Loan", description = "개인 신용 대출 상품 조회 API")
public class PersonalCreditLoanController {
    private final PersonalCreditLoanService service;

    @GetMapping
    public ResponseEntity<PersonalCreditLoanListResponse> list(
            @RequestParam(required = false) String sort, // "crdtGradAvg,DESC"(default)
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            PersonalCreditLoanListDataResponse data = service.getList(sort, page, size);
            return ResponseEntity.ok(
                    PersonalCreditLoanListResponse.builder().code(200).message("성공했습니다").data(data).build()
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    PersonalCreditLoanListResponse.builder().code(401).message("실패했습니다").data(null).build()
            );
        }
    }

    @GetMapping("/{personalCreditLoanId}")
    public ResponseEntity<PersonalCreditLoanDetailResponse> detail(@PathVariable("personalCreditLoanId") Long id) {
        try {
            PersonalCreditLoanDetailItemResponse item = service.getDetail(id);
            return ResponseEntity.ok(
                    PersonalCreditLoanDetailResponse.builder().code(200).message("성공했습니다").data(item).build()
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    PersonalCreditLoanDetailResponse.builder().code(401).message("실패했습니다").data(null).build()
            );
        }
    }
}

