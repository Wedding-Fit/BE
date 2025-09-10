package com.weddingfit.entity.goal;

import com.weddingfit.entity.deposit.DepositSavingType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "savings_goals")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long goalId;

    @Column(name = "couple_id", nullable = false)
    private Long coupleId;

    @Column(name = "financial_product_id")
    private Long financialProductId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoalCategory category;

    @Column(name = "goal_name", nullable = false, length = 100)
    private String goalName;

    @Column(name = "target_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentAmount;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "monthly_target", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyTarget;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_style", nullable = false, length = 20)
    private InvestmentStyle investmentStyle;

    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(precision = 15, scale = 2)
    private BigDecimal estimatedAmount;

    @Column(name = "notified_30", nullable = false)
    @Builder.Default
    private Boolean notified30 = false;

    @Column(name = "notified_60", nullable = false)
    @Builder.Default
    private Boolean notified60 = false;

    @Column(name = "notified_100", nullable = false)
    @Builder.Default
    private Boolean notified100 = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_status", nullable = false, length = 20)
    @Builder.Default
    private GoalStatus goalStatus = GoalStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    // -------------------- ENUM TYPES --------------------
    public enum GoalCategory {
        TRAVEL, WEDDING, HOUSING, CAR, EDU, FAMILY
    }

    public enum InvestmentStyle {
        DEPOSIT, SAVING, MIXED;

        public List<DepositSavingType> toDepositSavingTypes() {
            return switch (this) {
                case DEPOSIT -> List.of(DepositSavingType.DEPOSIT);
                case SAVING -> List.of(DepositSavingType.SAVING);
                case MIXED -> List.of(DepositSavingType.DEPOSIT, DepositSavingType.SAVING);
            };
        }
    }

    public enum GoalStatus {
        ACTIVE, COMPLETED
    }
}
