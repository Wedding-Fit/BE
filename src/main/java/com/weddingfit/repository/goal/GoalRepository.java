package com.weddingfit.repository.goal;

import com.weddingfit.entity.goal.Goal;
import com.weddingfit.entity.goal.Goal.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    // 특정 커플의 목표 목록 조회 (선택적으로 상태별 필터링)
    List<Goal> findByCoupleId(Long coupleId);

    List<Goal> findByCoupleIdAndGoalStatus(Long coupleId, GoalStatus goalStatus);


}