package com.weddingfit.dto.response.goal;

import com.weddingfit.entity.goal.Goal.GoalStatus;
import com.weddingfit.entity.goal.Goal.GoalCategory;
import com.weddingfit.entity.goal.Goal.InvestmentStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDetailResponse {
    private Long goalId;
    private Long coupleId;
    private GoalCategory category;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal monthlyTarget;
    private BigDecimal monthlyPayment;
    private InvestmentStyle investmentStyle;
    private GoalStatus goalStatus;
    private double progressPercent;
    private int totalMonth;
    private int remainingMonths;
    private ProductResponse product;
}