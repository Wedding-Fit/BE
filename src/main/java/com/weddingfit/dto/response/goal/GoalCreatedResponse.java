package com.weddingfit.dto.response.goal;

import com.weddingfit.entity.goal.Goal.GoalCategory;
import com.weddingfit.entity.goal.Goal.InvestmentStyle;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalCreatedResponse {

    @Builder
    @Getter
    @Setter
    public static class GoalInfo {
        private Long goalId;
        private GoalCategory category;
        private String goalName;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private LocalDate targetDate;
        private InvestmentStyle investmentStyle;
        private int progressPercent;
        private int monthsLeft;
        private BigDecimal monthlyNeeded;
    }

    private GoalInfo goal;
    private List<ProductResponse> products;
}
