package com.weddingfit.dto.response.goal;

import com.weddingfit.entity.goal.Goal.GoalStatus;
import com.weddingfit.entity.goal.Goal.GoalCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalListResponse {
    private Long goalId;
    private GoalCategory category;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private int progressPercent;
    private LocalDate startDate;
    private int totalMonth;
    private GoalStatus goalStatus;
}