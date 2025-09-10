package com.weddingfit.dto.request.goal;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalSaveProductRequest {
    private Long productId;
    private BigDecimal monthlyPayment;
    private BigDecimal estimatedAmount;
}
