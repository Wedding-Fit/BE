package com.weddingfit.service.goal;

import com.weddingfit.dto.request.goal.GoalCreateRequest;
import com.weddingfit.dto.request.goal.GoalSaveProductRequest;
import com.weddingfit.dto.response.goal.GoalCreatedResponse;
import com.weddingfit.dto.response.goal.GoalDetailResponse;
import com.weddingfit.dto.response.goal.GoalListResponse;

import java.util.List;

public interface GoalService {
    GoalCreatedResponse createGoal(GoalCreateRequest request);
    List<GoalListResponse> getGoalsByCoupleId(Long coupleId);
    GoalDetailResponse getGoalDetail(Long goalId);
    void saveGoalProduct(Long goalId, GoalSaveProductRequest request);
    void deleteGoal(Long goalId);
}