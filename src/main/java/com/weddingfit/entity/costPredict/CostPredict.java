package com.weddingfit.entity.costPredict;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "wedding_cost_predictions")
public class CostPredict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Long predictionId;

    @Column(name = "couple_id", nullable = false)
    private Long coupleId;

    @Column(name = "ceremony_cost", precision = 15, scale = 2)
    private BigDecimal ceremonyCost;

    @Column(name = "studio_cost", precision = 15, scale = 2)
    private BigDecimal studioCost;

    @Column(name = "food_cost", precision = 15, scale = 2)
    private BigDecimal foodCost;

    @Column(name = "dress_cost", precision = 15, scale = 2)
    private BigDecimal dressCost;

    @Column(name = "honeymoon_budget_cost", precision = 15, scale = 2)
    private BigDecimal honeymoonBudgetCost;

    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost;

    // 🔽🔽 추가: 프로필 지문 & 스냅샷
    @Column(name = "profile_fingerprint", length = 128)
    private String profileFingerprint;

    @Lob
    @Column(name = "profile_snapshot", columnDefinition = "TEXT")
    private String profileSnapshot;

    @Column(
            name = "predicted_at",
            insertable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime predictedAt;
}
