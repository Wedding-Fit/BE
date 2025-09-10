// GoalCreateRequest.java
package com.weddingfit.dto.request.goal;

import com.weddingfit.entity.goal.Goal.GoalCategory;
import com.weddingfit.entity.goal.Goal.InvestmentStyle;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalCreateRequest {
    private Long coupleId;
    private GoalCategory category;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private InvestmentStyle investmentStyle;
}