package com.weddingfit.controller.couple;

import com.weddingfit.dto.response.couple.CoupleSummaryResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.couple.CoupleSummaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/couples")
@Tag(name = "Couple Summary", description = "커플 정보 요약 API")
public class CoupleSummaryController {

    private final CoupleSummaryService service;

    @GetMapping("/summary/{coupleId}")
    public BaseResponse<CoupleSummaryResponse> getSummary(@PathVariable Long coupleId) {
        CoupleSummaryResponse dto = service.getSummary(coupleId);
        return BaseResponse.success(dto, "성공했습니다");
    }
}
