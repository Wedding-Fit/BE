package com.weddingfit.dto.response.costPredict;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostPredictDetailResponse {
    private int ceremonyRatio;
    private int foodRatio;
    private int studioRatio;
    private int dressRatio;
    private int honeymoonBudgetRatio;

    private long ceremonyCost;
    private long foodCost;
    private long studioCost;
    private long dressCost;
    private long honeymoonBudgetCost;
    private long totalCost;
}
