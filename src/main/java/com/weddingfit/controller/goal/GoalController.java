package com.weddingfit.controller.goal;

import com.weddingfit.dto.request.goal.GoalCreateRequest;
import com.weddingfit.dto.request.goal.GoalCurrentAmountUpdateRequest;
import com.weddingfit.dto.request.goal.GoalSaveProductRequest;
import com.weddingfit.dto.response.goal.GoalCreatedResponse;
import com.weddingfit.dto.response.goal.GoalDetailResponse;
import com.weddingfit.dto.response.goal.GoalListResponse;
import com.weddingfit.global.response.BaseResponse;
import com.weddingfit.service.goal.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "목표 저축", description = "목표 저축 관련 API")
@RestController
@RequestMapping("/api/goal")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    /**
     * 목표 생성 및 추천 상품 응답
     */
    @PostMapping("/input")
    @Operation(summary = "목표 생성 및 추천 상품 조회")
    public BaseResponse<GoalCreatedResponse> createGoal(@RequestBody GoalCreateRequest request) {
        GoalCreatedResponse response = goalService.createGoal(request);
        return BaseResponse.success(response);
    }

    /**
     * 커플 ID로 목표 리스트 조회
     */
    @GetMapping("/list/{coupleId}")
    @Operation(summary = "목표 리스트 조회 (커플 ID 기반)")
    public BaseResponse<List<GoalListResponse>> getGoalList(@PathVariable("coupleId") Long coupleId) {
        List<GoalListResponse> response = goalService.getGoalsByCoupleId(coupleId);
        return BaseResponse.success(response);
    }

    /**
     * 목표 상세 조회
     */
    @GetMapping("/detail/{goalId}")
    @Operation(summary = "목표 상세 조회")
    public BaseResponse<GoalDetailResponse> getGoalDetail(@PathVariable Long goalId) {
        GoalDetailResponse response = goalService.getGoalDetail(goalId);
        return BaseResponse.success(response);
    }

    /**
     * 목표에 금융 상품 저장
     */
    @PostMapping("/save/{goalId}")
    @Operation(summary = "목표에 금융 상품 저장")
    public BaseResponse<String> saveGoalProduct(
            @PathVariable Long goalId,
            @RequestBody GoalSaveProductRequest request
    ) {
        goalService.saveGoalProduct(goalId, request);
        return BaseResponse.success("금융 상품이 저장되었습니다.");
    }

    /**
     * 목표 현재 금액 업데이트 (FCM 알림 트리거)
     */
    @PutMapping("/update-amount/{goalId}")
    @Operation(summary = "목표 현재 금액 업데이트 및 FCM 알림 발송")
    public BaseResponse<String> updateCurrentAmount(
            @PathVariable Long goalId,
            @RequestBody GoalCurrentAmountUpdateRequest request
    ) {
        goalService.updateCurrentAmount(goalId, request);
        return BaseResponse.success("목표 금액이 업데이트되었습니다.");
    }

    /**
     * 목표 삭제
     */
    @DeleteMapping("/delete/{goalId}")
    @Operation(summary = "목표 삭제")
    public BaseResponse<String> deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return BaseResponse.success("목표가 성공적으로 삭제되었습니다.");
    }
}