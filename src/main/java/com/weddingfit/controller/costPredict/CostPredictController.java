package com.weddingfit.controller.costPredict;

import com.weddingfit.dto.response.costPredict.CostPredictDetailResponse;
import com.weddingfit.dto.response.costPredict.CostPredictResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.costPredict.CostPredictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/wedding-cost-predictions")
@Tag(name = "AI Wedding-Cost-Predict", description = "AI 결혼 비용 예측 API")
public class CostPredictController {

    private final CostPredictService costPredictService;

    @Operation(summary = "AI 결혼 비용 예측", description = "커플의 결혼 비용을 예측합니다.")
    @GetMapping("/{coupleId}")
    public ResponseEntity<BaseResponse<CostPredictResponse>> getRatios(@PathVariable Long coupleId) {
        CostPredictResponse data = costPredictService.getRatios(coupleId);
        return ResponseEntity.ok(BaseResponse.success(data, "성공했습니다"));
    }

    @Operation(summary = "AI 결혼 비용 예측 상세 정보", description = "커플의 결혼 비용 예측 상세 정보를 보여줍니다.")
    @GetMapping("/detail/{coupleId}")
    public ResponseEntity<BaseResponse<CostPredictDetailResponse>> getDetail(@PathVariable Long coupleId) {
        CostPredictDetailResponse data = costPredictService.getDetail(coupleId);
        return ResponseEntity.ok(BaseResponse.success(data, "결혼 비용 예측 조회에 성공했습니다"));
    }
}
